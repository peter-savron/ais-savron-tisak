package si.fri.ais.user.core.ports.outgoing.impl.local;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.ports.incoming.UserRepository;
import si.fri.ais.user.core.ports.incoming.UserKeyRepository;
import si.fri.ais.user.core.models.commands.UserLogin;
import si.fri.ais.user.core.models.commands.UserPasswordUpdate;
import si.fri.ais.user.core.ports.outgoing.UserAuthnService;

@IfBuildProfile("cli")
@ApplicationScoped
public class UserAuthnServiceCliImpl implements UserAuthnService {
    private final UserRepository userRepository;
    private final UserKeyRepository keyRepository;

    public UserAuthnServiceCliImpl(UserRepository userRepository, UserKeyRepository keyRepository) {
        this.userRepository = userRepository;
        this.keyRepository = keyRepository;
    }

    @Override
    public void login(UserLogin userLogin) {
        User user = userRepository.getUser(userLogin.getName())
                .orElseThrow(() -> new SecurityException("Invalid user name or password"));
        if (!UserPasswordHash.matches(userLogin.getPassword(), user.getPasswordHash())) {
            throw new SecurityException("Invalid user name or password");
        }
        try (var key = UserPasswordHash.deriveKey(userLogin.getPassword(), user.getKeySalt())) {
            keyRepository.storeKey(user.getName(), key);
        }
    }

    @Override
    public void logout(String userName) {
        // TODO sesion tracking will be implemented later
        keyRepository.removeKey(userName);
    }

    @Override
    public User updateUserPassword(UserPasswordUpdate passwordUpdate) {
        // TODO Create old key backup for disaster recovery
        // TODO rewrite secrets
        // TODO update password hash on User entity
        // TODO delete disaster recovery key
        return null;
    }
}
