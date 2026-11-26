package si.fri.ais.spec.core.ports.outgoing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.spec.core.models.Spec;
import si.fri.ais.spec.core.models.RawSpecRevision;
import si.fri.ais.spec.core.models.SpecDiff;
import si.fri.ais.spec.core.models.SpecSourceContext;
import si.fri.ais.workspace.core.models.WorkspaceContext;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides workspace-authorized access to published RawSpecs and their revision history.
 * Read operations return empty for inaccessible resources.
 * @see si.fri.ais.spec.core.exceptions.SpecSourceException
 */
public interface RawSpecService {

    List<Spec> publications(@Valid @NotNull SpecSourceContext context);
    /** List authorized publication metadata, optionally including soft-deleted records. */
    List<Spec> publications(@Valid @NotNull SpecSourceContext context, boolean includeDeleted);
    Optional<Spec> published(@Valid @NotNull WorkspaceContext context, @NotNull UUID rawId);
    /** Return normalized text for a visible publication in the authorized workspace. */
    /** Open normalized UTF-8 content for an authorized publication. The caller closes the stream. */
    Optional<InputStream> content(@Valid @NotNull WorkspaceContext context, @NotNull UUID rawId);
    /** Return visible publication history in revision order, with IDs usable for diff and publication timestamps. */
    Optional<List<RawSpecRevision>> getRevisions(@Valid @NotNull WorkspaceContext context, @NotNull UUID rawId);
    Optional<SpecDiff> diff(@Valid @NotNull WorkspaceContext context, @NotNull UUID rawId,
                            @NotBlank String base, @NotBlank String target);
}
