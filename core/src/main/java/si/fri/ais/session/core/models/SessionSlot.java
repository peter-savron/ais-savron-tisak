package si.fri.ais.session.core.models;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

/** Identifies a stored slot and, when available, the terminal shell instance. */
public record SessionSlot(@NotNull @Pattern(regexp = ID_FORMAT) String id, Instant shellStartedAt) {
    public static final String GLOBAL_ID = "_global";
    public static final String NAME_FORMAT = "[A-Za-z][A-Za-z0-9_-]{0,63}";
    public static final String SHARED_ID_FORMAT = GLOBAL_ID + "|" + NAME_FORMAT;
    private static final String TERMINAL_ID_FORMAT = "[1-9][0-9]*";
    public static final String ID_FORMAT = SHARED_ID_FORMAT + "|" + TERMINAL_ID_FORMAT;

    public boolean isTerminal() {
        return id != null && id.matches(TERMINAL_ID_FORMAT);
    }

    @AssertTrue(message = "Only terminal slots can carry a shell start time")
    public boolean isShellIdentityValid() {
        return shellStartedAt == null || isTerminal();
    }
}
