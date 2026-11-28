package si.fri.ais.user.core.models;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import si.fri.ais.common.core.models.NamedObject;

import java.util.UUID;

/** Selects one secret belonging to the acting user, by ID or by name. */
@Builder
public record UserSecretContext(
        @NotNull UUID userId,
        UUID secretId,
        @Pattern(regexp = NamedObject.NAME_FORMAT) String secretName) {

    /** Exactly one selector is required; names are unique within a user. */
    @AssertTrue(message = "Select a secret by either ID or name")
    public boolean isSelectorValid() {
        return (secretId != null) != (secretName != null);
    }
}
