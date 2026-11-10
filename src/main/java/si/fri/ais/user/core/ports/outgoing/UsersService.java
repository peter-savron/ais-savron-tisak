package si.fri.ais.user.core.ports.outgoing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.commmands.UserCreate;
import si.fri.ais.user.core.models.commmands.UserDelete;

import java.util.Optional;
import java.util.UUID;

/**
 * Used to manage app Users. Different implementations could handle online or local users.
 */
public interface UsersService {
    Optional<User> getUser(@NotNull UUID userId);
    Optional<User> getUser(@NotEmpty String userName);

    User createUser(@NotNull @Valid UserCreate create);
    // NOTE common update method will be added later when necessary
    User deleteUser(@NotNull @Valid UserDelete delete);
}
