package si.fri.ais.session.core.ports.incoming;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.session.core.models.Session;
import si.fri.ais.session.core.models.SessionSlot;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores local session slots. Each operation first commits cleanup of expired
 * sessions and their links, even if the requested operation fails. A record is
 * expired when its expiry is at or before the supplied time.
 */
public interface SessionRepository {
    /** Matches terminal shell identity when both the request and record provide it. */
    Optional<Session> find(@NotNull @Valid SessionSlot slot, @NotNull Instant now);

    /** Lists active named slots by descending last access, then ascending identifier. */
    List<Session> listNamed(@NotNull Instant now);

    /**
     * Inserts an identified session into a free slot or atomically replaces its
     * terminal record on confirmed PID reuse. Returns false for an occupied slot otherwise.
     */
    boolean create(@NotNull @Valid Session session, @NotNull Instant now);

    /**
     * Atomically replaces the expected active snapshots with the corresponding
     * updated snapshots. Both lists must contain the same distinct slot identifiers.
     * Returns false without applying replacements if any expected snapshot differs
     * from storage or is inactive. Use one call for operations affecting both a
     * terminal and a shared target.
     */
    boolean replace(
            @NotEmpty List<@NotNull @Valid Session> expected,
            @NotEmpty List<@NotNull @Valid Session> updated,
            @NotNull Instant now
    );

    /** Removes the matching slot and clears its links atomically. Returns false if absent after cleanup. */
    boolean remove(@NotNull @Valid SessionSlot slot, @NotNull Instant now);

    /** Removes sessions referencing a deleted workspace and clears their links atomically. */
    void removeByWorkspace(@NotNull UUID workspaceId, @NotNull Instant now);

    /** Removes the user's terminal slots on logout. */
    void removeTerminalByUser(@NotNull UUID userId, @NotNull Instant now);
}
