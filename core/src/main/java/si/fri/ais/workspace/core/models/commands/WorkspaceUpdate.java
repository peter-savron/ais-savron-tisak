package si.fri.ais.workspace.core.models.commands;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import si.fri.ais.workspace.core.models.Workspace;

/** Null metadata fields preserve their existing values. */
@Getter
@NoArgsConstructor
@SuperBuilder
public class WorkspaceUpdate {
    @Pattern(regexp = Workspace.NAME_FORMAT) private String name;
    private String title;
    @Size(max = Workspace.DESCRIPTION_LIMIT) private String description;
}
