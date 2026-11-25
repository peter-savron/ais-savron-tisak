package si.fri.ais.workspace.core.ports.incoming;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.workspace.core.exceptions.WorkspaceException;
import si.fri.ais.workspace.core.models.Workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository {
    Optional<Workspace> find(@NotNull UUID workspaceId);

    Optional<Workspace> findByName(@NotNull UUID ownerId, @NotNull String name);

    List<Workspace> listByOwner(@NotNull UUID ownerId);

    /** Creates the workspace atomically, reporting NAME_NOT_UNIQUE for an owner/name conflict. */
    void create(@NotNull @Valid Workspace workspace);

    /** Updates the workspace atomically, reporting NAME_NOT_UNIQUE for an owner/name conflict. */
    void update(@NotNull @Valid Workspace workspace);

    /**
     * Removes the workspace.
     * @throws WorkspaceException with DELETION_BLOCKED if remaining components prevent removal
     */
    void delete(@NotNull Workspace workspace);
}
