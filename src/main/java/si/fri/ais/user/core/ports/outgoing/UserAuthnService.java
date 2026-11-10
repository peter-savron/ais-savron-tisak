package si.fri.ais.user.core.ports.outgoing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.commmands.UserLogin;
import si.fri.ais.user.core.models.commmands.UserPasswordUpdate;

/**
 * Methods related to User authentication.
 */
public interface UserAuthnService {
    void login(@NotNull @Valid UserLogin userLogin);
    void logout(@NotEmpty String userName);
    User updateUserPassword(@NotNull @Valid UserPasswordUpdate passwordUpdate);
}
