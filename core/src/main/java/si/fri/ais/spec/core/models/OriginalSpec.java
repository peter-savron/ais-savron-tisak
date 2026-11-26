package si.fri.ais.spec.core.models;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import java.util.UUID;
import lombok.Setter;

/** Provenance of an observed upstream item. Identity is scoped to its source. */
@Getter
@SuperBuilder
@Setter
public class OriginalSpec {
    /** Source selection resolved by core while processing an upstream observation. */
    public enum Selection { SELECTED, MISSING, EXCLUDED, UNRESOLVED, ACCESS_DENIED }

    public enum ProcessingStatus { PENDING, DONE, FAILED, UNAVAILABLE }

    /** Stable UUID assigned by core when an item is first recorded. */
    private UUID id;

    /** Current source assessment, such as SELECTED or EXCLUDED. */
    @NotNull private Selection selection;

    /** Stable publication UUID, retained after removal. Null until the first successful publication. */
    private UUID rawSpecId;

    @NotNull private UUID sourceId;
    @NotBlank private String identity;
    @NotBlank private String locator;
    @NotBlank private String inputFormat;
    private String contentHash;
    private String revision;
    @NotBlank private String metadata;
    @NotNull private ProcessingStatus processingStatus;

    /** True when the repository read this snapshot while the source was syncing. */
    private boolean potentiallyStale;
}
