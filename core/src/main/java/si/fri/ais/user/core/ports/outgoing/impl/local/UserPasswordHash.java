package si.fri.ais.user.core.ports.outgoing.impl.local;

import org.bouncycastle.crypto.PasswordConverter;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import si.fri.ais.user.core.models.UserKey;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** Argon2id v1.3; authentication and AES derivation use independent salts. */
final class UserPasswordHash {
    // These parameters also define the persisted AES-key derivation scheme.
    // Changing them requires migrating encrypted secrets.
    private static final int MEMORY_KIB = 65_536;
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 1;
    private static final String PREFIX = "$argon2id$v=19$m=65536,t=3,p=1$";

    private UserPasswordHash() {
    }

    static String hash(char[] password) {
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derive(password, salt);
        try {
            return PREFIX + Base64.getEncoder().withoutPadding().encodeToString(salt)
                    + "$" + Base64.getEncoder().withoutPadding().encodeToString(hash);
        } finally {
            Arrays.fill(hash, (byte) 0);
        }
    }

    static boolean matches(char[] password, String encoded) {
        if (password == null || password.length == 0 || encoded == null || !encoded.startsWith(PREFIX)) {
            return false;
        }
        try {
            String[] parts = encoded.substring(PREFIX.length()).split("\\$", -1);
            if (parts.length != 2) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            if (salt.length != 32 || expected.length != 32) {
                return false;
            }
            byte[] actual = derive(password, salt);
            try {
                return MessageDigest.isEqual(expected, actual);
            } finally {
                Arrays.fill(actual, (byte) 0);
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static UserKey deriveKey(char[] password, byte[] keySalt) {
        return UserKey.init(derive(password, keySalt));
    }

    private static byte[] derive(char[] password, byte[] salt) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        if (salt == null || salt.length != 32) {
            throw new IllegalArgumentException("Salt must contain 32 bytes");
        }
        var builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(MEMORY_KIB).withIterations(ITERATIONS)
                .withParallelism(PARALLELISM).withSalt(salt);
        var parameters = builder.build();
        byte[] passwordBytes = PasswordConverter.UTF8.convert(password);
        byte[] result = new byte[32];
        try {
            var generator = new Argon2BytesGenerator();
            generator.init(parameters);
            generator.generateBytes(passwordBytes, result);
            return result;
        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
            parameters.clear();
            builder.clear();
        }
    }
}
