package si.fri.ais.workspace.core.ports.incoming;

import jakarta.validation.constraints.NotNull;
import si.fri.ais.workspace.core.models.Workspace;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository {
    Optional<Workspace> find(@NotNull UUID workspaceId);
}
