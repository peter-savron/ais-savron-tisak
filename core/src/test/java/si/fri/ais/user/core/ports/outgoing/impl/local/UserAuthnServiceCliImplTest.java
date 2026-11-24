package si.fri.ais.user.core.ports.outgoing.impl.local;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import si.fri.ais.user.core.models.UserKey;
import si.fri.ais.user.core.models.commands.UserCreate;
import si.fri.ais.user.core.models.commands.UserLogin;
import si.fri.ais.user.core.ports.incoming.UserKeyRepository;
import si.fri.ais.user.core.ports.incoming.UserRepository;
import java.util.ArrayList;
import java.util.List;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthnServiceCliImplTest {
    @Mock
    private UserRepository users;
    @Mock
    private UserKeyRepository keys;
    private UserAuthnServiceCliImpl service;

    @BeforeEach
    void setUp() {
        service = new UserAuthnServiceCliImpl(users, keys);
    }

    @Test
    void loginDerivesAesKeyOnlyAfterVerificationAndLogoutRemovesIt() {
        List<byte[]> storedKeys = new ArrayList<>();
        doAnswer(invocation -> {
            UserKey key = invocation.getArgument(1);
            storedKeys.add(key.useKey((java.util.function.Function<byte[], byte[]>) byte[]::clone));
            return null;
        }).when(keys).storeKey(eq("alice"), any(UserKey.class));
        var user = new UserServiceCliImpl(service, users).createUser(UserCreate.builder()
                .name("alice").password("secret".toCharArray()).build());
        when(users.getUser("alice")).thenReturn(Optional.of(user));
        assertThrows(SecurityException.class, () -> service.login(login("alice", "wrong")));
        assertThrows(SecurityException.class, () -> service.login(login("missing", "secret")));
        verifyNoInteractions(keys);
        service.login(login("alice", "secret"));
        byte[] first = storedKeys.getFirst();
        assertEquals(32, first.length);
        service.logout("alice");
        verify(keys).removeKey("alice");
        service.login(login("alice", "secret"));
        assertArrayEquals(first, storedKeys.get(1));
        try (var other = UserPasswordHash.deriveKey("different".toCharArray(), user.getKeySalt())) {
            other.useKey((java.util.function.Consumer<byte[]>) value -> assertFalse(Arrays.equals(first, value)));
        }
        byte[] salt = user.getKeySalt().clone();
        salt[0] ^= 1;
        try (var other = UserPasswordHash.deriveKey("secret".toCharArray(), salt)) {
            other.useKey((java.util.function.Consumer<byte[]>) value -> assertFalse(Arrays.equals(first, value)));
        }
        var capturedKeys = ArgumentCaptor.forClass(UserKey.class);
        verify(keys, times(2)).storeKey(eq("alice"), capturedKeys.capture());
        for (UserKey key : capturedKeys.getAllValues()) {
            assertThrows(IllegalStateException.class,
                    () -> key.useKey((java.util.function.Consumer<byte[]>) value -> fail("Key must be closed")));
        }
    }

    private UserLogin login(String name, String password) {
        return UserLogin.builder().name(name).password(password.toCharArray()).build();
    }
}
