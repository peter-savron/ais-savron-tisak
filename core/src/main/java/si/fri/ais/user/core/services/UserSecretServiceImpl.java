package si.fri.ais.user.core.services;

import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.user.core.exceptions.UserSecretException;
import si.fri.ais.user.core.models.StoredUserSecret;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.UserKey;
import si.fri.ais.user.core.models.UserSecret;
import si.fri.ais.user.core.models.UserSecretContext;
import si.fri.ais.user.core.models.UserSecretMetadata;
import si.fri.ais.user.core.models.commands.UserSecretCreate;
import si.fri.ais.user.core.models.commands.UserSecretGet;
import si.fri.ais.user.core.models.commands.UserSecretUpdate;
import si.fri.ais.user.core.ports.incoming.UserKeyRepository;
import si.fri.ais.user.core.ports.incoming.UserRepository;
import si.fri.ais.user.core.ports.incoming.UserSecretRepository;
import si.fri.ais.user.core.ports.outgoing.UserAuthnService;
import si.fri.ais.user.core.ports.outgoing.UserSecretCipher;
import si.fri.ais.user.core.ports.outgoing.UserSecretService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static si.fri.ais.user.core.exceptions.UserSecretException.Code.INVALID_PASSWORD;
import static si.fri.ais.user.core.exceptions.UserSecretException.Code.KEY_UNAVAILABLE;
import static si.fri.ais.user.core.exceptions.UserSecretException.Code.NAME_NOT_UNIQUE;
import static si.fri.ais.user.core.exceptions.UserSecretException.Code.OWNER_NOT_FOUND;
import static si.fri.ais.user.core.exceptions.UserSecretException.Code.SECRET_NOT_FOUND;
import static si.fri.ais.user.core.exceptions.UserSecretException.Code.USER_LOGGED_OUT;

/** Authorizes owner-scoped operations and keeps plaintext out of repository calls. */
@ApplicationScoped
public class UserSecretServiceImpl implements UserSecretService {
    private final UserSecretRepository secrets;
    private final UserRepository users;
    private final UserKeyRepository keys;
    private final UserAuthnService authentication;
    private final UserSecretCipher cipher;

    public UserSecretServiceImpl(UserSecretRepository secrets, UserRepository users,
                                 UserKeyRepository keys, UserAuthnService authentication,
                                 UserSecretCipher cipher) {
        this.secrets = secrets;
        this.users = users;
        this.keys = keys;
        this.authentication = authentication;
        this.cipher = cipher;
    }

    @Override
    public List<UserSecretMetadata> list(UUID userId) {
        requireUser(userId);
        return secrets.list(userId).stream()
                .filter(secret -> userId.equals(secret.getOwner()))
                .sorted(Comparator.comparing(StoredUserSecret::getName))
                .map(this::metadata)
                .toList();
    }

    @Override
    public Optional<UserSecret> getSecret(UserSecretGet get) {
        User user = requireUser(get.getContext().userId());
        try {
            authentication.verifyPassword(user.getId(), get.getPassword());
        } catch (SecurityException failure) {
            throw new UserSecretException(INVALID_PASSWORD, failure);
        }
        try (UserKey key = requireKey(user)) {
            return find(get.getContext()).map(secret -> UserSecret.builder()
                    .id(secret.getId())
                    .name(secret.getName())
                    .owner(secret.getOwner())
                    .type(secret.getType())
                    .secret(cipher.decrypt(secret, key))
                    .build());
        }
    }

    @Override
    public UserSecretMetadata createSecret(UserSecretCreate create) {
        User user = requireUser(create.getUserId());
        try (UserKey key = requireKey(user)) {
            if (secrets.getSecret(user.getId(), create.getSecretName()).isPresent()) {
                throw new UserSecretException(NAME_NOT_UNIQUE);
            }
            StoredUserSecret secret = StoredUserSecret.builder()
                    .id(UUID.randomUUID())
                    .name(create.getSecretName())
                    .owner(user.getId())
                    .type(create.getType())
                    .build();
            cipher.encrypt(secret, create.getSecret(), key);
            secrets.createSecret(secret);
            return metadata(secret);
        }
    }

    @Override
    public UserSecretMetadata updateSecret(UserSecretUpdate update) {
        User user = requireUser(update.getContext().userId());
        try (UserKey key = requireKey(user)) {
            StoredUserSecret secret = find(update.getContext())
                    .orElseThrow(() -> new UserSecretException(SECRET_NOT_FOUND));
            secret.setType(update.getType());
            cipher.encrypt(secret, update.getSecret(), key);
            secrets.updateSecret(secret);
            return metadata(secret);
        }
    }

    @Override
    public void deleteSecret(UserSecretContext context) {
        requireUser(context.userId());
        secrets.deleteSecret(find(context).orElseThrow(() -> new UserSecretException(SECRET_NOT_FOUND)));
    }

    private Optional<StoredUserSecret> find(UserSecretContext context) {
        return (context.secretId() != null ? secrets.getSecret(context.secretId())
                : secrets.getSecret(context.userId(), context.secretName()))
                .filter(secret -> context.userId().equals(secret.getOwner()));
    }

    private User requireUser(UUID userId) {
        User user = users.getUser(userId).orElseThrow(() -> new UserSecretException(OWNER_NOT_FOUND));
        if (!user.isLogged()) {
            throw new UserSecretException(USER_LOGGED_OUT);
        }
        return user;
    }

    private UserKey requireKey(User user) {
        try {
            return keys.getKey(user.getName()).orElseThrow(() -> new UserSecretException(KEY_UNAVAILABLE));
        } catch (UserSecretException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            throw new UserSecretException(KEY_UNAVAILABLE, failure);
        }
    }

    private UserSecretMetadata metadata(StoredUserSecret secret) {
        return UserSecretMetadata.builder().id(secret.getId()).owner(secret.getOwner())
                .name(secret.getName()).type(secret.getType()).build();
    }
}
