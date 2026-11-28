package si.fri.ais.user.core.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.common.core.models.NamedObject;

import java.util.UUID;
import java.util.Arrays;

/** Plaintext revealed to a trusted caller; close after use and never expose it to agents. */
@Getter
@Setter
@SuperBuilder
public class UserSecret extends NamedObject implements AutoCloseable {
    /**
     * Owner of the secret.
     */
    @NotNull private UUID owner;
    /**
     * Decrypted user secret.
     */
    @NotNull private byte[] secret;

    /**
     * User secret type.
     */
    @NotNull private Type type;

    public enum Type {
        BYTES,
        TEXT,
        INTEGER,
        DECIMAL
    }

    /** Clears the revealed bytes after the trusted caller has consumed them. */
    @Override
    public void close() {
        if (secret != null) {
            Arrays.fill(secret, (byte) 0);
        }
    }
}
