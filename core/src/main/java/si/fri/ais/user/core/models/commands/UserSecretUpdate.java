package si.fri.ais.user.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import si.fri.ais.user.core.models.UserSecret;
import si.fri.ais.user.core.models.UserSecretContext;

@Getter
@NoArgsConstructor
@SuperBuilder
public class UserSecretUpdate {
    /** Identifies the acting user and the existing secret to replace in place. */
    @NotNull @Valid private UserSecretContext context;
    /**
     * Replacement plaintext; the caller owns and clears this array.
     */
    @NotNull
    private byte[] secret;

    /**
     * User secret type.
     */
    @NotNull
    private UserSecret.Type type;
}
