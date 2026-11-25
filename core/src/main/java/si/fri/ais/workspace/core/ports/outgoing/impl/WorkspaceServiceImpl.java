package si.fri.ais.workspace.core.ports.outgoing.impl;

import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.user.core.ports.outgoing.UserService;
import si.fri.ais.workspace.core.exceptions.WorkspaceException;
import si.fri.ais.workspace.core.models.Workspace;
import si.fri.ais.workspace.core.models.WorkspaceContext;
import si.fri.ais.workspace.core.models.commands.WorkspaceCreate;
import si.fri.ais.workspace.core.models.commands.WorkspaceUpdate;
import si.fri.ais.workspace.core.ports.incoming.WorkspaceRepository;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static si.fri.ais.workspace.core.exceptions.WorkspaceException.Code.NAME_NOT_UNIQUE;
import static si.fri.ais.workspace.core.exceptions.WorkspaceException.Code.OWNER_NOT_FOUND;
import static si.fri.ais.workspace.core.exceptions.WorkspaceException.Code.WORKSPACE_NOT_FOUND;

@ApplicationScoped
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceRepository repository;
    private final UserService users;

    public WorkspaceServiceImpl(WorkspaceRepository repository, UserService users) {
        this.repository = repository;
        this.users = users;
    }

    @Override
    public Workspace create(UUID userId, WorkspaceCreate command) {
        users.getUser(userId).orElseThrow(() -> new WorkspaceException(OWNER_NOT_FOUND));
        if (repository.findByName(userId, command.getName()).isPresent()) {
            throw new WorkspaceException(NAME_NOT_UNIQUE);
        }
        Workspace workspace = Workspace.builder()
                .id(UUID.randomUUID())
                .ownerId(userId)
                .name(command.getName())
                .title(command.getTitle())
                .description(command.getDescription())
                .build();
        repository.create(workspace);
        return workspace;
    }

    @Override
    public Workspace update(WorkspaceContext context, WorkspaceUpdate command) {
        Workspace workspace = requireWorkspace(context);
        Optional.ofNullable(command.getName()).filter(name -> !name.equals(workspace.getName())).ifPresent(name -> {
            if (repository.findByName(context.userId(), name).isPresent()) {
                throw new WorkspaceException(NAME_NOT_UNIQUE);
            }
            workspace.setName(name);
        });
        Optional.ofNullable(command.getTitle()).ifPresent(workspace::setTitle);
        Optional.ofNullable(command.getDescription()).ifPresent(workspace::setDescription);
        repository.update(workspace);
        return workspace;
    }

    @Override
    public void delete(WorkspaceContext context) {
        Workspace workspace = requireWorkspace(context);
        repository.delete(workspace);
    }

    @Override
    public Optional<Workspace> getWorkspace(WorkspaceContext context) {
        return repository.find(context.workspaceId())
                .filter(workspace -> context.userId().equals(workspace.getOwnerId()));
    }

    @Override
    public Optional<Workspace> getWorkspace(UUID userId, String name) {
        return repository.findByName(userId, name);
    }

    @Override
    public List<Workspace> list(UUID userId) {
        return repository.listByOwner(userId);
    }

    private Workspace requireWorkspace(WorkspaceContext context) {
        return getWorkspace(context).orElseThrow(() -> new WorkspaceException(WORKSPACE_NOT_FOUND));
    }
}
