package si.fri.ais.session.core.ports.outgoing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.session.core.models.ResolvedSession;
import si.fri.ais.session.core.models.Session;
import si.fri.ais.session.core.models.commands.SessionDelete;
import si.fri.ais.session.core.models.commands.SessionGet;
import si.fri.ais.session.core.models.commands.SessionResolve;
import si.fri.ais.session.core.models.commands.SessionSet;
import si.fri.ais.session.core.models.commands.SessionStart;
import si.fri.ais.session.core.models.commands.SessionStop;
import si.fri.ais.session.core.models.commands.SessionUse;

import java.util.List;
import java.util.Optional;

public interface SessionService {
    /** Validates the preferences and initializes timestamps. An occupied slot requires confirmed PID reuse. */
    Session start(@NotNull @Valid SessionStart command);

    /**
     * Returns empty for an inactive slot. Requires login for the selected user
     * and refreshes a usable session before returning it.
     */
    Optional<Session> get(@NotNull @Valid SessionGet command);

    /** Validates the resulting preferences and refreshes the updated session. */
    Session set(@NotNull @Valid SessionSet command);

    /** Updates the link and refreshes the terminal and selected target as one operation. */
    ResolvedSession use(@NotNull @Valid SessionUse command);

    /**
     * Returns empty if the selected slot is inactive. An unavailable linked target is
     * reported as a failure. All required users must be logged in before anything is refreshed.
     */
    Optional<ResolvedSession> resolveDefaults(@NotNull @Valid SessionResolve command);

    /** Removes the active slot and clears links to it, reporting inactive slots as failures. */
    void stop(@NotNull @Valid SessionStop command);

    /** Removes the named slot and clears links to it, reporting missing names as failures. */
    void delete(@NotNull @Valid SessionDelete command);

    /**
     * Lists active named sessions by descending last access, then ascending identifier.
     * When usableOnly is true, includes only sessions eligible for selection by use.
     * Listing leaves last access and expiry untouched.
     */
    List<Session> listNamed(boolean usableOnly);
}
