package si.fri.ais.user.core.models.commmands;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.SuperBuilder;
import si.fri.ais.user.core.models.UserSecret;

@Getter
@SuperBuilder
public class UserSecretCreate {
    @NotEmpty
    private String ownerName;
    @NotEmpty
    private String secretName;
    @NotNull private byte[] secret;

    /**
     * User secret type.
     */
    @NotNull private UserSecret.Type type;
}
