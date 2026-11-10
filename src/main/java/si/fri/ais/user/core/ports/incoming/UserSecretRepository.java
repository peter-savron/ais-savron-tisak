package si.fri.ais.user.core.ports.incoming;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.UserKey;
import si.fri.ais.user.core.models.UserSecret;

import java.util.Optional;
import java.util.UUID;

public interface UserSecretRepository {
    Optional<UserSecret> getSecret(@NotNull UUID secretId);
    Optional<UserSecret> getSecret(@NotEmpty String secretName);

    /**
     * Reencrypts dynamic encryption keys with new master key
     * @param userId user id
     * @param old old master encryption key
     * @param current new master encryption key
     */
    void updateKek(@NotNull UUID userId, @NotNull UserKey old, @NotNull UserKey current);

    void createSecret(@NotNull UserSecret secret);
    void updateSecret(@NotNull @Valid UserSecret secret);
    void deleteSecret(@NotNull @Valid UserSecret secret);
}
