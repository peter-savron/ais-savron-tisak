package si.fri.ais.workspace.core.ports.outgoing;

import jakarta.validation.constraints.NotNull;
import si.fri.ais.workspace.core.models.Workspace;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceService {
    /** Returns the workspace by ID, or empty if the ID is unknown. */
    Optional<Workspace> getWorkspace(@NotNull UUID workspaceId);
}
