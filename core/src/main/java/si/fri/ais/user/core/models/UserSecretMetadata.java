package si.fri.ais.user.core.models;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/** Public description of a secret; contains neither plaintext nor ciphertext. */
@Builder
public record UserSecretMetadata(
        @NotNull UUID id,
        @NotNull UUID owner,
        @NotNull String name,
        @NotNull UserSecret.Type type) {
}
