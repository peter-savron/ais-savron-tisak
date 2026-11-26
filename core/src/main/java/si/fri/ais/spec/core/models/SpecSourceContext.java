package si.fri.ais.spec.core.models;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/** Identifies an existing spec source within the acting user's workspace. */
@Builder
public record SpecSourceContext(@NotNull UUID userId, @NotNull UUID workspaceId, @NotNull UUID sourceId) {}
