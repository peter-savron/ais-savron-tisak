package si.fri.ais.spec.core.models;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import java.time.Instant;
import java.util.UUID;
import lombok.Setter;

/** An upstream item and its optional normalized publication. */
@Getter
@SuperBuilder
@Setter
public class Spec {
    /**
     * NEW, UPDATED, MOVED and REINTEGRATED remain in effect until processing succeeds and sets UP_TO_DATE.
     * DELETED marks an item confirmed absent from its source; UNAVAILABLE marks a read or processing failure.
     */
    public enum Status {
        UP_TO_DATE, DELETED, EXCLUDED, UNRESOLVED, ACCESS_DENIED,
        UNAVAILABLE, NEW, UPDATED, NO_CHANGE, MOVED, REINTEGRATED
    }

    public enum ProcessingStatus { PENDING, DONE, FAILED, UNAVAILABLE }

    /** Stable UUID assigned by core when an item is first recorded. */
    private UUID id;

    /** Current discovery and processing state. */
    @NotNull private Status status;

    @NotNull private UUID sourceId;
    /**
     * Provider-assigned key used to match this document across scans of the same source.
     * For local files, this is the source-relative path, such as api/orders.md.
     */
    @NotBlank private String externalId;

    /** Upstream hash associated with this spec. */
    private String externalHash;
    /** Display filename supplied by the provider, when available. */
    private String fileName;

    /** Upstream modification timestamp, when available. */
    private Instant updatedAt;
    private String inputFormat;
    private String contentHash;
    /** Hash of the processed document. */
    private String processedHash;
    private String revision;
    @NotBlank private String metadata;
    @NotNull private ProcessingStatus processingStatus;

    /** True when the repository read this snapshot while the source was syncing. */
    private boolean potentiallyStale;

    /** True when application queries should limit access to this Spec; synchronization preserves this setting. */
    @Builder.Default
    private boolean ignored = false;

    /** True when the last publication has been retired from ordinary reads. */
    private boolean deleted;
}
