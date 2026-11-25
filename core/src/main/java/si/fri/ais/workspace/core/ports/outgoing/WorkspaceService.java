package si.fri.ais.workspace.core.ports.outgoing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import si.fri.ais.workspace.core.models.Workspace;
import si.fri.ais.workspace.core.models.WorkspaceContext;
import si.fri.ais.workspace.core.models.commands.WorkspaceCreate;
import si.fri.ais.workspace.core.models.commands.WorkspaceUpdate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceService {
    Workspace create(@NotNull UUID userId, @NotNull @Valid WorkspaceCreate command);

    Workspace update(@NotNull @Valid WorkspaceContext context, @NotNull @Valid WorkspaceUpdate command);

    void delete(@NotNull @Valid WorkspaceContext context);

    /** Returns empty if the workspace is missing or owned by another user. */
    Optional<Workspace> getWorkspace(@NotNull @Valid WorkspaceContext context);

    Optional<Workspace> getWorkspace(@NotNull UUID userId,
                                     @NotNull @Pattern(regexp = Workspace.NAME_FORMAT) String name);

    List<Workspace> list(@NotNull UUID userId);
}
