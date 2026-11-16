package si.fri.ais.user.core.ports.outgoing.impl.local;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.commmands.UserCreate;
import si.fri.ais.user.core.models.commmands.UserDelete;
import si.fri.ais.user.core.ports.outgoing.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@IfBuildProfile("cli")
@ApplicationScoped
public class UserServiceCliImpl implements UserService {
    @Override
    public List<User> list() {
        // TODO getAll via repo
        return List.of();
    }

    @Override
    public Optional<User> getUser(UUID userId) {
        // TOdo get via repo
        return Optional.empty();
    }

    @Override
    public Optional<User> getUser(String userName) {
        return Optional.empty();
    }

    @Override
    public User createUser(UserCreate create) {
        return null;
    }

    @Override
    public User deleteUser(UserDelete delete) {
        // TODO Check password and delete
        return null;
    }
}
