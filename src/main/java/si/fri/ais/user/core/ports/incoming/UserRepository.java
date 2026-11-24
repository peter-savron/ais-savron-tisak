package si.fri.ais.user.core.ports.incoming;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    /** Returns users ordered by name, starting at the zero-based offset. */
    List<User> getAll(int offset, int limit);
    Optional<User> getUser(@NotNull UUID userId);
    Optional<User> getUser(@NotEmpty String userName);
    void createUser(@NotNull User user);
    /** Updates the mutable fields of the user identified by its ID. */
    void updateUser(@NotNull @Valid User user);
    void removeUser(@NotNull UUID userId);
}
