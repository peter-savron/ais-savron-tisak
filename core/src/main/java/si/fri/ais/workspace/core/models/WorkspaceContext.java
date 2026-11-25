package si.fri.ais.workspace.core.models;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Identifies the acting user and the workspace targeted by an operation. */
public record WorkspaceContext(@NotNull UUID userId, @NotNull UUID workspaceId) {
}
