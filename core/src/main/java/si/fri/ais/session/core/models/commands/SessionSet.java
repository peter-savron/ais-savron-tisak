package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.session.core.models.SessionSlot;

import java.util.UUID;

/** Updates the supplied preferences. Null values preserve the current preferences. */
@Getter
@SuperBuilder
public class SessionSet {
    @NotNull @Valid private final SessionSlot slot;
    private final UUID userId;
    private final UUID workspaceId;

    @AssertTrue(message = "Supply a user or workspace to update")
    public boolean isUpdatePresent() {
        return userId != null || workspaceId != null;
    }
}
