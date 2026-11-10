package si.fri.ais.user.core.ports.incoming;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.User;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepository {
    Optional<User> getUser(@NotNull UUID userId);
    Optional<User> getUser(@NotEmpty String userName);
    void createUser(@NotNull User user);
    void updateUser(@NotNull @Valid User user);
    void removeUser(@NotNull UUID userId);
}
