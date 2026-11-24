package si.fri.ais.user.core.ports.incoming;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.UserKey;

import java.util.Optional;

/**
 * Retrieves Keys used to encrypt user secrets
 */
public interface UserKeyRepository {
    /**
     * Retrieves users cryptographic key.
     *
     * @param userName user for which to retrieve the cryptographic key.
     * @return the cryptographic key.
     */
    Optional<UserKey> getKey(@NotEmpty String userName);

    /**
     * Stores key to the defined key storage.
     * @param userName owner.
     * @param userKey key.
     */
    void storeKey(@NotEmpty String userName, @NotNull UserKey userKey);

    /**
     * Wipes the key completely, invalidating all bytes.
     * @param userName user for which the key was stored.
     */
    void removeKey(@NotEmpty String userName);
}
