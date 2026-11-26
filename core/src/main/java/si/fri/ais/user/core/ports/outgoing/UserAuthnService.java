package si.fri.ais.user.core.ports.outgoing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.commands.UserLogin;
import si.fri.ais.user.core.models.commands.UserPasswordUpdate;

import java.util.UUID;

/**
 * Methods related to User authentication.
 */
public interface UserAuthnService {
    void login(@NotNull @Valid UserLogin userLogin);
    /** Verifies credentials without modifying login state or stored keys. */
    void verifyPassword(@NotNull UUID userId, @NotNull char[] password);

    void logout(@NotEmpty String userName);
    User updateUserPassword(@NotNull @Valid UserPasswordUpdate passwordUpdate);
}
