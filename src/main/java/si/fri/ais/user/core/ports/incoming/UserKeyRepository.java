package si.fri.ais.user.core.ports.incoming;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.UserKey;

/**
 * Retrieves Keys used to encrypt user secrets
 */
public interface UserKeyRepository {
    UserKey getKey(@NotEmpty String userName);
    void storeKey(@NotNull UserKey userKey);
    void removeKey(@NotEmpty String userName);
}
