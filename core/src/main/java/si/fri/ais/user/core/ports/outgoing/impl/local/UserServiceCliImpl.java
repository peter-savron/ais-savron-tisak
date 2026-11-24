package si.fri.ais.user.core.ports.outgoing.impl.local;

import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.user.core.exceptions.UserException;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.commands.UserCreate;
import si.fri.ais.user.core.models.commands.UserDelete;
import si.fri.ais.user.core.ports.incoming.UserRepository;
import si.fri.ais.user.core.ports.outgoing.UserAuthnService;
import si.fri.ais.user.core.ports.outgoing.UserService;

import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static si.fri.ais.user.core.exceptions.UserException.Code.USER_CREATE_NAME_NOT_UNIQUE;

@ApplicationScoped
public class UserServiceCliImpl implements UserService {
    private final UserAuthnService userAuthnService;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public UserServiceCliImpl(UserAuthnService userAuthnService, UserRepository userRepository) {
        this.userAuthnService = userAuthnService;
        this.userRepository = userRepository;
    }

    @Override
    public List<User> list() {
        return userRepository.getAll(0, Integer.MAX_VALUE);
    }

    @Override
    public Optional<User> getUser(UUID userId) {
        return userRepository.getUser(userId);
    }

    @Override
    public Optional<User> getUser(String userName) {
        return userRepository.getUser(userName);
    }

    @Override
    public User createUser(UserCreate create) {
        if (userRepository.getUser(create.getName()).isPresent()) {
            throw new UserException(USER_CREATE_NAME_NOT_UNIQUE);
        }
        byte[] keySalt = new byte[32];
        random.nextBytes(keySalt);
        User user = User.builder()
                .id(UUID.randomUUID())
                .name(create.getName())
                .passwordHash(UserPasswordHash.hash(create.getPassword()))
                .keySalt(keySalt)
                .build();
        userRepository.createUser(user);
        return user;
    }

    @Override
    public void deleteUser(UserDelete delete) {
        userAuthnService.logout(delete.getName());
        User user = userRepository.getUser(delete.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + delete.getName()));
        if (!UserPasswordHash.matches(delete.getPassword(), user.getPasswordHash())) {
            throw new SecurityException("Invalid password");
        }
        userRepository.removeUser(user.getId());
    }
}
