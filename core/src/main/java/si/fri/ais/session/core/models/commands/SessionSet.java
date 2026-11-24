package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.session.core.models.SessionSlot;

import java.util.UUID;

/** Updates the supplied preferences. Null values preserve the current preferences. */
public record SessionSet(@NotNull @Valid SessionSlot slot, UUID userId, UUID workspaceId) {
    @AssertTrue(message = "Supply a user or workspace to update")
    public boolean isUpdatePresent() {
        return userId != null || workspaceId != null;
    }
}
