package si.fri.ais.user.integrations.local.impl;

import com.arcadedb.database.Database;
import com.arcadedb.database.MutableDocument;
import com.arcadedb.query.sql.executor.ResultSet;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import si.fri.ais.common.core.config.StorageConfig;
import si.fri.ais.common.integration.local.arcadedb.ArcadeDbChangelogRepository;
import si.fri.ais.common.integration.local.arcadedb.scripts.CommonArcadeDbScripts;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.ports.incoming.UserRepository;
import si.fri.ais.user.integrations.local.mappers.UserDocumentMapper;
import si.fri.ais.user.integrations.local.values.UserArcadeDbValues;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static si.fri.ais.user.integrations.local.values.UserArcadeDbValues.userDocumentType;

@JBossLog
@ApplicationScoped
public class UserRepositoryLocalImpl implements UserRepository {

    private final StorageConfig storageConfig;
    private final ArcadeDbChangelogRepository arcadeDbChangelogRepository;
    private final Database database;
    private final UserDocumentMapper userDocumentMapper;

    public UserRepositoryLocalImpl(
            StorageConfig storageConfig,
            ArcadeDbChangelogRepository arcadeDbChangelogRepository,
            UserDocumentMapper userDocumentMapper,
            Database database
    ) {
        this.storageConfig = storageConfig;
        this.arcadeDbChangelogRepository = arcadeDbChangelogRepository;
        this.userDocumentMapper = userDocumentMapper;
        this.database = database;
    }

    @PostConstruct
    void init() {
        arcadeDbChangelogRepository.runChangelogs(UserArcadeDbValues.userSchemaChangelogs);
    }

    @Override
    public List<User> getAll(int offset, int limit) {
        if (offset < 0 || limit < 0) {
            throw new IllegalArgumentException("Offset and limit must not be negative");
        }
        if (limit == 0) {
            return List.of();
        }
        List<User> users = new ArrayList<>();
        String query = "SELECT FROM %s ORDER BY name SKIP ? LIMIT ?".formatted(userDocumentType);
        try (ResultSet results = database.query("sql", query, offset, limit)) {
            while (results.hasNext()) {
                results.next().getElement().map(userDocumentMapper::fromDocument).ifPresent(users::add);
            }
        }
        return users;
    }

    @Override
    public Optional<User> getUser(UUID userId) {
        return CommonArcadeDbScripts.searchById(
                database,
                userDocumentType,
                userId
        ).map(userDocumentMapper::fromDocument);
    }

    @Override
    public Optional<User> getUser(String userName) {
        return CommonArcadeDbScripts.searchByName(
                database,
                userDocumentType,
                userName,
                userDocumentMapper::fromDocument
        );
    }

    @Override
    public void createUser(User user) {
        Path userDir = storageConfig.dataPath().resolve(user.getId().toString());
        try {
            Files.createDirectories(userDir);
        } catch (Exception e) {
            log.errorf(e, "Failed to create data directory for user: %s", user.getName());
            throw new RuntimeException("Failed to create user", e);
        }

        try {
            database.transaction(() -> {
                MutableDocument doc = database.newDocument(userDocumentType);
                userDocumentMapper.toDocument(user, doc).save();
            });
        } catch (Exception e) {
            log.errorf(e, "Failed to store new User to DB: %s", user.getName());
            deleteDirectoryRecursively(userDir);
            throw new RuntimeException("Could not create User", e);
        }
    }

    @Override
    public void updateUser(User user) {
        database.transaction(() ->
                CommonArcadeDbScripts.updateById(
                        database,
                        userDocumentType,
                        user.getId(),
                        doc -> userDocumentMapper.toDocument(user, doc)
                )
        );
    }

    @Override
    public void removeUser(UUID userId) {
        database.transaction(() -> CommonArcadeDbScripts.deleteById(
                database,
                userDocumentType,
                userId
        ));

        Path userDir = storageConfig.dataPath().resolve(userId.toString());
        deleteDirectoryRecursively(userDir);
    }

    // TODO probably better to move to common
    private void deleteDirectoryRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            log.errorf(e, "Failed to recursively delete user directory: %s", dir);
            throw new UncheckedIOException(e);
        }
    }
}
