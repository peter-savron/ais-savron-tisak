package si.fri.ais.spec.core.services;

import si.fri.ais.spec.core.exceptions.SpecSourceBusyException;
import si.fri.ais.spec.core.exceptions.SpecSourceException;
import si.fri.ais.spec.core.exceptions.SpecSourceException.Code;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import si.fri.ais.spec.core.models.*;
import si.fri.ais.spec.core.models.commands.*;
import si.fri.ais.spec.core.models.SpecSyncResult;
import si.fri.ais.spec.core.models.SpecSource.SyncEndState;
import si.fri.ais.spec.core.ports.outgoing.*;
import si.fri.ais.user.core.ports.outgoing.UserService;
import si.fri.ais.workspace.core.models.WorkspaceContext;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;
import java.util.*;

@ApplicationScoped
@RequiredArgsConstructor
public class SpecSourceServiceImpl implements SpecSourceService {
    private final SpecSourceRepository sources;
    private final SpecRepository specs;
    private final WorkspaceService workspaces;
    private final UserService users;
    private final SpecProviders providers;
    private final SpecSynchronizer synchronizer;

    public List<String> availableTypes() {
        return providers.types();
    }

    public String describeConfiguration(String type) {
        return providers.get(type).describeConfiguration();
    }

    public SpecSource create(WorkspaceContext context, SpecSourceCreate input) {
        users.getUser(context.userId()).orElseThrow(() -> new SpecSourceException(Code.USER_UNAVAILABLE, "User unavailable"));
        var source = SpecSource.builder().workspaceId(requireWorkspace(context)).id(UUID.randomUUID())
                .name(input.getName()).type(input.getType()).status(SpecSource.Status.ACTIVE)
                .root(input.getRoot()).subfolder(input.getSubfolder()).selectionRules(List.copyOf(input.getSelectionRules())).build();
        providers.get(source.getType()).validate(source);
        sources.create(source);
        return source;
    }

    public Optional<SpecSource> get(SpecSourceContext context) {
        return workspace(workspaceContext(context)).flatMap(id -> sources.source(context.sourceId())
                .filter(source -> id.equals(source.getWorkspaceId())));
    }

    public List<SpecSource> list(WorkspaceContext context) {
        return sources.sources(requireWorkspace(context));
    }

    public void update(SpecSourceUpdate input) {
        var context = input.getContext();
        sources.update(requireSourceId(context), source -> {
            requireActive(source);
            Optional.ofNullable(input.getRoot()).ifPresent(source::setRoot);
            Optional.ofNullable(input.getSubfolder()).ifPresent(source::setSubfolder);
            if (input.isClearSubfolder()) source.setSubfolder(null);
            Optional.ofNullable(input.getSelectionRules()).map(List::copyOf).ifPresent(source::setSelectionRules);
            providers.get(source.getType()).validate(source);
        });
    }

    public void delete(SpecSourceContext context) {
        var sourceId = requireSourceId(context);
        sources.delete(sourceId, () -> specs.deleteSourceSpecs(sourceId));
    }

    public SpecSyncResult sync(SpecSourceContext context) {
        var sourceId = requireSourceId(context);
        sources.update(sourceId, source -> {
            requireActive(source);
            source.setStatus(SpecSource.Status.SYNCING);
        });
        try {
            var source = sources.source(sourceId).orElseThrow(() ->
                    new SpecSourceException(Code.SOURCE_UNAVAILABLE, "Claimed source unavailable"));
            var result = synchronizer.process(source);
            SyncEndState endState = syncEndState(result);
            sources.update(sourceId, sourceToComplete -> {
                if (sourceToComplete.getStatus() != SpecSource.Status.SYNCING) {
                    throw new SpecSourceBusyException("Sync no longer owns the source");
                }
                sourceToComplete.setLastSyncResult(endState);
                sourceToComplete.setStatus(SpecSource.Status.ACTIVE);
            });
            source.setLastSyncResult(endState);
            source.setStatus(SpecSource.Status.ACTIVE);
            return result;
        } catch (RuntimeException | Error failure) {
            try {
                sources.update(sourceId, sourceToAbort -> {
                    if (sourceToAbort.getStatus() == SpecSource.Status.SYNCING) {
                        sourceToAbort.setStatus(SpecSource.Status.ACTIVE);
                    }
                });
            } catch (RuntimeException | Error abortFailure) {
                failure.addSuppressed(abortFailure);
            }
            throw failure;
        }
    }

    private SyncEndState syncEndState(SpecSyncResult result) {
        boolean partialFailure = result.specs().stream().anyMatch(original ->
                original.getStatus() == Spec.Status.ACCESS_DENIED
                        || original.getProcessingStatus() == Spec.ProcessingStatus.FAILED);
        if (partialFailure) {
            return SyncEndState.PARTIAL_FAILURE;
        }
        if (result.specs().stream()
                .anyMatch(original -> original.getStatus() == Spec.Status.UNRESOLVED)) {
            return SyncEndState.SUCCESS_UNRESOLVABLE_SOURCES;
        }
        return SyncEndState.SUCCESS;
    }

    public List<Spec> items(SpecSourceContext context) {
        return get(context).map(source -> specs.items(source.getId())).orElseGet(List::of);
    }

    private WorkspaceContext workspaceContext(SpecSourceContext context) {
        return new WorkspaceContext(context.userId(), context.workspaceId());
    }

    private Optional<UUID> workspace(WorkspaceContext context) {
        return workspaces.getWorkspace(context).map(w -> context.workspaceId());
    }

    private UUID requireWorkspace(WorkspaceContext context) {
        return workspace(context).orElseThrow(() -> new SpecSourceException(Code.WORKSPACE_UNAVAILABLE, "Workspace unavailable"));
    }

    private UUID requireSourceId(SpecSourceContext context) {
        requireWorkspace(workspaceContext(context));
        return sources.source(context.sourceId())
                .filter(source -> context.workspaceId().equals(source.getWorkspaceId()))
                .map(SpecSource::getId)
                .orElseThrow(() -> new SpecSourceException(Code.SOURCE_UNAVAILABLE, "Source unavailable"));
    }

    private void requireActive(SpecSource source) {
        if (source.getStatus() != SpecSource.Status.ACTIVE) {
            throw new SpecSourceBusyException("Source is " + source.getStatus());
        }
    }

}
