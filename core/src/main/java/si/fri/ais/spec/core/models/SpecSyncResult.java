package si.fri.ais.spec.core.models;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.util.List;

/**
 * Committed source and OriginalSpecs processed by one synchronization.
 *
 * @param source full source whose items were reconciled
 * @param originalSpecs discovered or retried OriginalSpecs with their resulting statuses
 */
@Builder
public record SpecSyncResult(@NotNull SpecSource source, List<OriginalSpec> originalSpecs) {
    public SpecSyncResult {
        originalSpecs = List.copyOf(originalSpecs);
    }
}
