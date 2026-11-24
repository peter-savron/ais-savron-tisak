package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.session.core.models.SessionSlot;

import java.util.UUID;

/** Starts a session for the selected user, with an optional workspace and stored slot. */
public record SessionStart(@NotNull UUID userId, UUID workspaceId, @Valid SessionSlot slot) {
    public SessionStart(UUID userId) {
        this(userId, null, null);
    }
}
