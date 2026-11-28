package si.fri.ais.user.integrations.local.impl;

import com.arcadedb.database.Database;
import com.arcadedb.database.Document;
import com.arcadedb.database.MutableDocument;
import com.arcadedb.exception.DuplicatedKeyException;
import com.arcadedb.query.sql.executor.ResultSet;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.common.integration.local.arcadedb.ArcadeDbChangelogRepository;
import si.fri.ais.user.core.exceptions.UserSecretException;
import si.fri.ais.user.core.models.StoredUserSecret;
import si.fri.ais.user.core.models.UserKey;
import si.fri.ais.user.core.ports.incoming.UserSecretRepository;
import si.fri.ais.user.core.ports.outgoing.UserSecretCipher;
import si.fri.ais.user.integrations.local.mappers.UserSecretDocumentMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static si.fri.ais.user.core.exceptions.UserSecretException.Code.NAME_NOT_UNIQUE;
import static si.fri.ais.user.core.exceptions.UserSecretException.Code.SECRET_NOT_FOUND;
import static si.fri.ais.user.core.exceptions.UserSecretException.Code.STORAGE_FAILURE;
import static si.fri.ais.user.integrations.local.values.UserSecretArcadeDbValues.*;

/** Transactional local persistence for encrypted user-secret envelopes. */
@ApplicationScoped
public class UserSecretRepositoryImpl implements UserSecretRepository {
    private final ArcadeDbChangelogRepository changelogs;
    private final Database database;
    private final UserSecretDocumentMapper mapper;
    private final UserSecretCipher cipher;

    public UserSecretRepositoryImpl(ArcadeDbChangelogRepository changelogs, Database database,
                                    UserSecretDocumentMapper mapper, UserSecretCipher cipher) {
        this.changelogs = changelogs;
        this.database = database;
        this.mapper = mapper;
        this.cipher = cipher;
    }

    @PostConstruct
    void init() {
        access(() -> {
            changelogs.runChangelogs(CHANGELOGS);
            return null;
        });
    }

    @Override
    public Optional<StoredUserSecret> getSecret(UUID secretId) {
        return access(() -> select("SELECT FROM " + DOCUMENT_TYPE + " WHERE " + ID + " = ?",
                secretId.toString()).stream().findFirst().map(mapper::fromDocument));
    }

    @Override
    public Optional<StoredUserSecret> getSecret(UUID userId, String secretName) {
        return access(() -> select("SELECT FROM " + DOCUMENT_TYPE + " WHERE " + OWNER + " = ? AND " + NAME + " = ?",
                userId.toString(), secretName).stream().findFirst().map(mapper::fromDocument));
    }

    @Override
    public List<StoredUserSecret> list(UUID userId) {
        return access(() -> select("SELECT FROM " + DOCUMENT_TYPE + " WHERE " + OWNER + " = ? ORDER BY " + NAME,
                userId.toString()).stream().map(mapper::fromDocument).toList());
    }

    @Override
    public void createSecret(StoredUserSecret secret) {
        write(() -> {
            MutableDocument document = database.newDocument(DOCUMENT_TYPE);
            mapper.initialize(secret, document);
            document.save();
        });
    }

    @Override
    public void updateSecret(StoredUserSecret secret) {
        write(() -> {
            MutableDocument document = requireDocument(secret).modify();
            mapper.update(secret, document);
            document.save();
        });
    }

    @Override
    public void deleteSecret(StoredUserSecret secret) {
        write(() -> database.deleteRecord(requireDocument(secret)));
    }

    @Override
    public void updateKek(UUID userId, UserKey old, UserKey current) {
        write(() -> select("SELECT FROM " + DOCUMENT_TYPE + " WHERE " + OWNER + " = ?", userId.toString())
                .forEach(document -> {
                    StoredUserSecret secret = mapper.fromDocument(document);
                    cipher.rewrap(secret, old, current);
                    MutableDocument mutable = document.modify();
                    mapper.update(secret, mutable);
                    mutable.save();
                }));
    }

    private Document requireDocument(StoredUserSecret secret) {
        return select("SELECT FROM " + DOCUMENT_TYPE + " WHERE " + ID + " = ? AND " + OWNER + " = ? AND " + NAME + " = ?",
                secret.getId().toString(), secret.getOwner().toString(), secret.getName())
                .stream().findFirst().orElseThrow(() -> new UserSecretException(SECRET_NOT_FOUND));
    }

    private List<Document> select(String sql, Object... parameters) {
        try (ResultSet results = database.query("sql", sql, parameters)) {
            return results.stream().map(result -> result.getElement().orElseThrow()).toList();
        }
    }

    private void write(Runnable operation) {
        access(() -> {
            database.transaction(operation::run);
            return null;
        });
    }

    private <T> T access(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (UserSecretException failure) {
            throw failure;
        } catch (DuplicatedKeyException failure) {
            throw new UserSecretException(NAME_NOT_UNIQUE, failure);
        } catch (RuntimeException failure) {
            throw new UserSecretException(STORAGE_FAILURE, failure);
        }
    }
}
