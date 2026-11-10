package si.fri.ais.user.core.ports.outgoing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.UserSecret;
import si.fri.ais.user.core.models.commmands.UserSecretCreate;
import si.fri.ais.user.core.models.commmands.UserSecretDelete;
import si.fri.ais.user.core.models.commmands.UserSecretUpdate;

import java.util.Optional;
import java.util.UUID;

/**
 * Used to manage User secrets.
 */
public interface UserSecretService {
    Optional<UserSecret> getSecret(@NotNull UUID secretId);
    Optional<UserSecret> getSecret(@NotEmpty String userName, @NotNull String secretName);

    UserSecret createSecret(@NotNull @Valid UserSecretCreate create);
    UserSecret updateSecret(@NotNull @Valid UserSecretUpdate update);
    void deleteSecret(@NotNull @Valid UserSecretDelete delete);
}
