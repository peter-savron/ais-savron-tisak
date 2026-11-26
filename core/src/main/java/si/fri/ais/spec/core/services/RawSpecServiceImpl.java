package si.fri.ais.spec.core.services;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import si.fri.ais.spec.core.models.*;
import si.fri.ais.spec.core.ports.outgoing.RawSpecService;
import si.fri.ais.spec.core.ports.outgoing.SpecRepository;
import si.fri.ais.spec.core.ports.outgoing.SpecSourceRepository;
import si.fri.ais.workspace.core.models.WorkspaceContext;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class RawSpecServiceImpl implements RawSpecService {
    private final SpecRepository specs;
    private final SpecSourceRepository sources;
    private final WorkspaceService workspaces;


    @Override
    public List<RawSpec> publications(SpecSourceContext context) {
        return publications(context, false);
    }

    @Override
    public List<RawSpec> publications(SpecSourceContext context, boolean includeDeleted) {
        return source(context).map(source -> specs.publications(source.getId(), includeDeleted)).orElseGet(List::of);
    }

    @Override
    public Optional<RawSpec> published(WorkspaceContext context, UUID rawId) {
        return workspace(context).flatMap(workspaceId -> workspacePublication(workspaceId, rawId))
                .flatMap(raw -> specs.published(raw.getId()));
    }

    @Override
    public Optional<String> content(WorkspaceContext context, UUID rawId) {
        return workspace(context).flatMap(workspaceId -> workspacePublication(workspaceId, rawId))
                .flatMap(raw -> specs.content(raw.getId()));
    }

    @Override
    public Optional<List<RawSpecRevision>> getRevisions(WorkspaceContext context, UUID rawId) {
        return workspace(context).flatMap(workspaceId -> workspacePublication(workspaceId, rawId))
                .flatMap(raw -> specs.getRevisions(raw.getId()));
    }

    @Override
    public Optional<SpecDiff> diff(WorkspaceContext context, UUID rawId, String base, String target) {
        return workspace(context).flatMap(workspaceId -> workspacePublication(workspaceId, rawId))
                .flatMap(raw -> specs.diff(raw.getId(), base, target));
    }

    private Optional<UUID> workspace(WorkspaceContext context) {
        return workspaces.getWorkspace(context).map(workspace -> context.workspaceId());
    }

    private Optional<SpecSource> source(SpecSourceContext context) {
        WorkspaceContext workspaceContext = new WorkspaceContext(context.userId(), context.workspaceId());
        return workspace(workspaceContext).flatMap(workspaceId -> sources.source(context.sourceId())
                .filter(source -> workspaceId.equals(source.getWorkspaceId())));
    }

    private Optional<RawSpec> workspacePublication(UUID workspaceId, UUID rawId) {
        return specs.get(rawId).filter(raw -> sources.source(raw.getSourceId())
                .filter(source -> workspaceId.equals(source.getWorkspaceId()))
                .isPresent());
    }
}
