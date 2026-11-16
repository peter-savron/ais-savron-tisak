package si.fri.ais.user.integrations.local.impl;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import si.fri.ais.common.core.config.StorageConfig;
import si.fri.ais.user.core.models.UserKey;
import si.fri.ais.user.core.ports.incoming.UserKeyRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Optional;

@JBossLog
@ApplicationScoped
public class UserKeyRepositoryLocalImpl implements UserKeyRepository {

    private final Path userKeysDir;

    public UserKeyRepositoryLocalImpl(StorageConfig storageConfig) {
        try {
            this.userKeysDir = storageConfig.cachePath().resolve(".user-keys");
            Files.createDirectories(this.userKeysDir);
        } catch (Exception e) {
            log.error("Could not initialize UserKeys folder.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserKey> getKey(String userName) {
        Path keyFile = resolveUserKeyPath(userName);
        if (!Files.exists(keyFile)) {
            return Optional.empty();
        }
        try {
            return Optional.of(UserKey.init(Files.readAllBytes(keyFile)));
        } catch (IOException e) {
            log.errorf(e, "Could not read key file for user: %s", userName);
            throw new RuntimeException("Could not read key file", e);
        }
    }

    @Override
    public void storeKey(String userName, UserKey userKey) {
        Path keyFile = resolveUserKeyPath(userName);
        userKey.useKey(key -> {
            try {
                Files.write(
                        keyFile,
                        key,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                );
            } catch (IOException e) {
                throw new RuntimeException("Could not write key", e);
            }
        });
    }

    @Override
    public void removeKey(String userName) {
        Path keyFile = resolveUserKeyPath(userName);
        if (!Files.exists(keyFile)) {
            return;
        }
        try {
            long fileSize = Files.size(keyFile);
            if (fileSize > 0) {
                byte[] randomData = new byte[(int) fileSize];
                new SecureRandom().nextBytes(randomData);
                Files.write(keyFile, randomData, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
            }
            Files.deleteIfExists(keyFile);
        } catch (IOException e) {
            log.errorf(e, "Could not securely remove key file for user: %s", userName);
            throw new RuntimeException(e);
        }
    }

    private Path resolveUserKeyPath(String userName) {
        Path resolved = userKeysDir.resolve(userName).normalize();
        if (!resolved.startsWith(userKeysDir)) {
            throw new IllegalArgumentException("Invalid userName path: " + userName);
        }
        return resolved;
    }
}
