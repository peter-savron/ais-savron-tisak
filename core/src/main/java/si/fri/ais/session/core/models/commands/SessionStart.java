package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.session.core.models.SessionSlot;

import java.util.UUID;

/** Starts a session for the selected user, with an optional workspace and stored slot. */
@Getter
@SuperBuilder
public class SessionStart {
    @NotNull private final UUID userId;
    private final UUID workspaceId;
    @Valid private final SessionSlot slot;
}
