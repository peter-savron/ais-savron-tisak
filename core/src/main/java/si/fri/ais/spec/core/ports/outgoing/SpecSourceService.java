package si.fri.ais.spec.core.ports.outgoing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import si.fri.ais.spec.core.models.*;
import si.fri.ais.spec.core.models.commands.*;
import si.fri.ais.spec.core.models.SpecSyncResult;
import si.fri.ais.workspace.core.models.WorkspaceContext;
import java.util.*;

/**
 * Manages workspace-scoped source registrations and synchronization.
 * Required workspace and user lookups report WORKSPACE_UNAVAILABLE or USER_UNAVAILABLE.
 * Mutations report SOURCE_UNAVAILABLE for an inaccessible source and SOURCE_BUSY for a conflicting claim.
 * Configuration failures expose field violations. Duplicate registrations report NAME_CONFLICT.
 * @see si.fri.ais.spec.core.exceptions.SpecSourceException
 */
public interface SpecSourceService {

    List<String> availableTypes();
    String describeConfiguration(@NotBlank String type);

    SpecSource create(@Valid @NotNull WorkspaceContext context, @Valid @NotNull SpecSourceCreate input);
    Optional<SpecSource> get(@Valid @NotNull SpecSourceContext context);
    List<SpecSource> list(@Valid @NotNull WorkspaceContext context);
    void update(@Valid @NotNull SpecSourceUpdate input);
    void delete(@Valid @NotNull SpecSourceContext context);

    SpecSyncResult sync(@Valid @NotNull SpecSourceContext context);
    List<OriginalSpec> items(@Valid @NotNull SpecSourceContext context);
}
