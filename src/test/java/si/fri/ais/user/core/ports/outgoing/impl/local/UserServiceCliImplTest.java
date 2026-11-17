package si.fri.ais.user.core.ports.outgoing.impl.local;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.commmands.UserCreate;
import si.fri.ais.user.core.models.commmands.UserDelete;
import si.fri.ais.user.core.ports.incoming.UserRepository;
import si.fri.ais.user.core.ports.outgoing.UserAuthnService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceCliImplTest {
    @Mock
    private UserRepository repository;
    @Mock
    private UserAuthnService authentication;
    private UserServiceCliImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceCliImpl(authentication, repository);
    }

    @Test
    void createsUsersAndDelegatesListingAndLookups() {
        assertTrue(service.list().isEmpty());
        User user = create("alice");
        User other = create("bob");
        assertTrue(user.getPasswordHash().startsWith("$argon2id$v=19$m=65536,t=3,p=1$"));
        assertNotNull(user.getId());
        assertEquals("alice", user.getName());
        assertEquals(32, user.getKeySalt().length);
        assertFalse(java.util.Arrays.equals(user.getKeySalt(), other.getKeySalt()));
        assertNotEquals(user.getPasswordHash(), other.getPasswordHash());
        assertTrue(UserPasswordHash.matches("secret".toCharArray(), user.getPasswordHash()));
        verify(repository).createUser(user);
        verify(repository).createUser(other);
        when(repository.getAll(0, Integer.MAX_VALUE)).thenReturn(List.of(user, other));
        when(repository.getUser(user.getId())).thenReturn(Optional.of(user));
        when(repository.getUser("alice")).thenReturn(Optional.of(user));
        assertEquals(List.of(user, other), service.list());
        assertSame(user, service.getUser(user.getId()).orElseThrow());
        assertSame(user, service.getUser("alice").orElseThrow());
        assertTrue(service.getUser("missing").isEmpty());
        assertTrue(service.getUser(UUID.randomUUID()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> create("alice"));
        verify(repository, times(2)).createUser(any(User.class));
    }

    @Test
    void deletionRequiresCorrectPasswordAndExistingUser() {
        User user = create("alice");
        when(repository.getUser("alice")).thenReturn(Optional.of(user));
        assertThrows(SecurityException.class, () -> service.deleteUser(delete("alice", "wrong")));
        verify(repository, never()).removeUser(any(UUID.class));
        assertThrows(NoSuchElementException.class, () -> service.deleteUser(delete("missing", "secret")));
        service.deleteUser(delete("alice", "secret"));
        verify(repository).removeUser(user.getId());
    }

    @Test
    void rejectsMalformedHashesAndEmptyPasswords() {
        for (String hash : List.of("", "plaintext", "pbkdf2-sha256$0$bad$bad",
                "pbkdf2-sha256$600000$bad$bad", "pbkdf2-sha256$invalid$x$x", "$argon2id$v=19$m=65536,t=3,p=1$bad$bad",
                "$argon2id$v=19$m=999999999,t=3,p=1$bad$bad")) {
            assertFalse(UserPasswordHash.matches("secret".toCharArray(), hash));
        }
        assertThrows(IllegalArgumentException.class, () -> UserPasswordHash.hash(new char[0]));
    }

    private User create(String name) {
        return service.createUser(UserCreate.builder().name(name).password("secret".toCharArray()).build());
    }

    private UserDelete delete(String name, String password) {
        return UserDelete.builder().name(name).password(password.toCharArray()).build();
    }
}
