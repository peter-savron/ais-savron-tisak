package si.fri.ais.user.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import si.fri.ais.user.core.models.UserSecretContext;
import si.fri.ais.user.core.models.validation.ValidUserPassword;

/** Explicit plaintext reveal. Passwords and revealed values must never reach agents. */
@Getter
@NoArgsConstructor
@SuperBuilder
public class UserSecretGet {
    /** Acting user and secret selector. */
    @NotNull @Valid private UserSecretContext context;
    /** Freshly supplied credentials; the caller owns and clears this array. */
    @NotNull @ValidUserPassword private char[] password;
}
