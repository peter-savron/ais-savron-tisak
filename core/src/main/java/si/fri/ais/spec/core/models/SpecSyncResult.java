package si.fri.ais.spec.core.models;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.util.List;

/**
 * Committed source and Specs processed by one synchronization.
 *
 * @param source full source whose items were reconciled
 * @param specs discovered or retried Specs with their resulting statuses
 */
@Builder
public record SpecSyncResult(@NotNull SpecSource source, List<Spec> specs) {
    public SpecSyncResult {
        specs = List.copyOf(specs);
    }
}
