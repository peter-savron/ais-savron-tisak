package si.fri.ais.workspace.core.ports.outgoing.impl;

import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.workspace.core.models.Workspace;
import si.fri.ais.workspace.core.ports.incoming.WorkspaceRepository;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceRepository repository;

    public WorkspaceServiceImpl(WorkspaceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Workspace> getWorkspace(UUID workspaceId) {
        return repository.find(workspaceId);
    }
}
