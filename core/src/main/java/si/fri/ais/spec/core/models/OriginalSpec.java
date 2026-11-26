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
    /**
     * Discovery and processing state for this upstream item. NEW and CHANGED are provider observations;
     * SELECTED means selected input still needs processing or processing failed; UP_TO_DATE means
     * synchronization and processing succeeded. The remaining values describe availability or scope.
     */
    public enum Status { UP_TO_DATE, SELECTED, MISSING, EXCLUDED, UNRESOLVED, ACCESS_DENIED, NEW, CHANGED }

    public enum ProcessingStatus { PENDING, DONE, FAILED, UNAVAILABLE }

    /** Stable UUID assigned by core when an item is first recorded. */
    private UUID id;

    /** Current source assessment, such as SELECTED or EXCLUDED. */
    /** Current discovery and processing state. */
    @NotNull private Status status;

    /** Stable publication UUID, reserved before the first publication write and retained after removal. */
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
