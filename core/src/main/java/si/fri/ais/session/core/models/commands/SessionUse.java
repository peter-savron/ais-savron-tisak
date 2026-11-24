package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import si.fri.ais.session.core.models.SessionSlot;

/** Switches the terminal to a named or global session. A null target selects its own preferences. */
public record SessionUse(
        @NotNull @Valid SessionSlot terminal,
        @Pattern(regexp = SessionSlot.SHARED_ID_FORMAT) String targetId
) {
    @AssertTrue(message = "Session use requires a terminal slot")
    public boolean isTerminalSlot() {
        return terminal == null || terminal.isTerminal();
    }
}
