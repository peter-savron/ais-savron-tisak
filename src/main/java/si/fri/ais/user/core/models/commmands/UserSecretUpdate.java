package si.fri.ais.user.core.models.commmands;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.SuperBuilder;
import si.fri.ais.common.core.models.NamedObject;
import si.fri.ais.user.core.models.UserSecret;

import java.util.UUID;

@Getter
@SuperBuilder
public class UserSecretUpdate extends NamedObject {
    /**
     * Owner of the secret.
     */
    @NotNull
    private UUID owner;
    /**
     * Decrypted user secret.
     */
    @NotNull
    private byte[] secret;

    /**
     * User secret type.
     */
    @NotNull
    private UserSecret.Type type;
}
