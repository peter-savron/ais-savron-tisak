package si.fri.ais.spec.core.ports.outgoing;

import si.fri.ais.spec.core.exceptions.SpecSourceNameConflictException;
import si.fri.ais.spec.core.exceptions.SpecSourceBusyException;
import si.fri.ais.spec.core.models.SpecSource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Stores source registrations, lifecycle state and sync results.
 * Mutations requiring an existing registration report
 * {@link si.fri.ais.spec.core.exceptions.SpecSourceException.Code#SOURCE_UNAVAILABLE}
 * through a SpecSourceException if the source ID is missing.
 */
public interface SpecSourceRepository {
    /**
     * Enforce workspace/name uniqueness through final deletion.
     * @throws SpecSourceNameConflictException if the workspace already reserves this name
     */
    void create(SpecSource source);
    Optional<SpecSource> source(UUID sourceId);
    List<SpecSource> sources(UUID workspaceId);

    /**
     * Apply the core edit to the current source atomically and discard it if it throws.
     * Serialize this write with sync item writes. Persist status and the latest sync
     * result together. A rejected edit leaves the stored state intact.
     */
    void update(UUID sourceId, Consumer<SpecSource> edit);

    /**
     * Mark and fence the source, run ordered cleanup outside the source transaction,
     * then remove the registration. Preserve the marked source if cleanup fails.
     * @throws SpecSourceBusyException if another operation holds the source claim
     */
    void delete(UUID sourceId, Runnable cleanup);
}
