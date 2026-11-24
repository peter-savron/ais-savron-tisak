package si.fri.ais.user.core.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.SuperBuilder;
import si.fri.ais.common.core.models.NamedObject;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class UserSecret extends NamedObject {
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
}
