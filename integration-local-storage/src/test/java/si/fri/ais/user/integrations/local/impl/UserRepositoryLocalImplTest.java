package si.fri.ais.user.integrations.local.impl;

import com.arcadedb.database.DatabaseFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.integrations.local.mappers.UserDocumentMapper;
import si.fri.ais.user.integrations.local.values.UserArcadeDbValues;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryLocalImplTest {
    @TempDir Path directory;

    @Test
    void listsMappedUsersInNameOrderWithPagination() {
        try (var factory = new DatabaseFactory(directory.resolve("users").toString());
             var database = factory.create()) {
            database.getSchema().createDocumentType(UserArcadeDbValues.userDocumentType);
            UserDocumentMapper mapper = new UserDocumentMapper();
            database.transaction(() -> {
                for (String name : List.of("charlie", "alice", "bob")) {
                    User user = User.builder().id(UUID.randomUUID()).name(name)
                            .passwordHash("hash").keySalt(new byte[]{1, 2}).build();
                    mapper.toDocument(user, database.newDocument(UserArcadeDbValues.userDocumentType)).save();
                }
            });
            var repository = new UserRepositoryLocalImpl(null, null, mapper, database);
            assertEquals(List.of("alice", "bob", "charlie"),
                    repository.getAll(0, Integer.MAX_VALUE).stream().map(User::getName).toList());
            var page = repository.getAll(1, 1);
            assertEquals("bob", page.getFirst().getName());
            assertNotNull(page.getFirst().getId());
            assertEquals("hash", page.getFirst().getPasswordHash());
            assertArrayEquals(new byte[]{1, 2}, page.getFirst().getKeySalt());
            assertTrue(repository.getAll(10, 2).isEmpty());
            assertTrue(repository.getAll(0, 0).isEmpty());
            assertThrows(IllegalArgumentException.class, () -> repository.getAll(-1, 1));
            assertThrows(IllegalArgumentException.class, () -> repository.getAll(0, -1));
        }
    }
}
