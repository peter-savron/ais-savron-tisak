package si.fri.ais.workspace.core.models.commands;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import si.fri.ais.workspace.core.models.Workspace;

@Getter
@NoArgsConstructor
@SuperBuilder
public class WorkspaceCreate {
    @NotNull @Pattern(regexp = Workspace.NAME_FORMAT) private String name;
    private String title;
    @Size(max = Workspace.DESCRIPTION_LIMIT) private String description;
}
