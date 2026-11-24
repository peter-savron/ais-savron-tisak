package si.fri.ais.workspace.core.ports.outgoing;

import jakarta.validation.constraints.NotNull;
import si.fri.ais.workspace.core.models.Workspace;

import java.util.UUID;

public interface WorkspaceService {
    /** Retrieves the workspace by ID and throws if it cannot be retrieved. */
    Workspace getWorkspace(@NotNull UUID workspaceId);
}
