package si.fri.ais.user.core.models.commands;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import si.fri.ais.common.core.models.NamedObject;
import si.fri.ais.user.core.models.UserSecret;

import java.util.UUID;

@Getter
@NoArgsConstructor
@SuperBuilder
public class UserSecretCreate {
    /** Acting user and owner of the new secret. */
    @NotNull private UUID userId;
    /** Name, for example OPENAI_API_KEY; unique within this user. */
    @NotNull @Pattern(regexp = NamedObject.NAME_FORMAT) private String secretName;
    /** Plaintext supplied by the trusted caller, who owns and clears this array. */
    @NotNull private byte[] secret;

    /**
     * User secret type.
     */
    @NotNull private UserSecret.Type type;
}
