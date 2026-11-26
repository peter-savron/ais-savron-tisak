package si.fri.ais.spec.core.models;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import java.util.UUID;
import lombok.Setter;

/** Last successful publication, retained across soft deletion and restoration. */
@Getter
@SuperBuilder
@Setter
public class RawSpec {
    @NotNull private UUID id;
    @NotNull private UUID sourceId;
    @NotBlank private String identity;
    @NotBlank private String locator;
    @NotBlank private String inputFormat;
    @NotBlank private String contentHash;
    private String revision;
    @NotBlank private String metadata;
    /** MIME type of the normalized text, such as text/markdown or text/plain. */
    @NotBlank private String outputFormat;
    private boolean deleted;

    /** True when the repository read this snapshot while the source was syncing. */
    private boolean potentiallyStale;
}
