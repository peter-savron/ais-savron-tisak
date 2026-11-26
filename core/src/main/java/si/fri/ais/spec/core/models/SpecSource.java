package si.fri.ais.spec.core.models;

import jakarta.validation.constraints.*;
import lombok.Getter;
import si.fri.ais.common.core.models.NamedObject;
import lombok.experimental.SuperBuilder;
import java.util.UUID;
import java.util.List;
import lombok.Setter;

/** A named upstream selection owned by one workspace. */
@Getter
@SuperBuilder
@Setter
public class SpecSource {
    public enum Status { ACTIVE, SYNCING, MARKED_FOR_DELETION }

    public enum SyncEndState { SUCCESS, PARTIAL_FAILURE, SUCCESS_UNRESOLVABLE_SOURCES }

    public boolean isPotentiallyStale() {
        return status == Status.SYNCING;
    }

    @NotNull private UUID id;
    @NotNull private UUID workspaceId;
    @NotNull @Pattern(regexp = NamedObject.NAME_FORMAT) private String name;
    @NotNull private Status status;
    /** End state of the latest committed sync, or null before the first committed sync. */
    private SyncEndState lastSyncResult;

    @NotBlank private String type;
    @NotBlank private String root;
    private String subfolder;
    @NotNull private List<@NotBlank String> selectionRules;
}
