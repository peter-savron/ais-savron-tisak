package si.fri.ais.session.core.ports.incoming;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import si.fri.ais.session.core.exceptions.SessionException;
import si.fri.ais.session.core.models.Session;
import si.fri.ais.session.core.models.SessionSlot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores local session slots. Each operation first commits cleanup of expired
 * sessions and their links, even if the requested operation fails. A record is
 * expired when its expiry is at or before the operation time.
 * Models returned by reads are detached. Mutating them must affect storage
 * only after a successful write.
 */
public interface SessionRepository {
    /** Matches terminal shell identity when both the request and record provide it. */
    Optional<Session> find(@NotNull @Valid SessionSlot slot);

    /** Lists the user's active named slots by descending last access, then ascending identifier. */
    List<Session> listNamed(@NotNull UUID userId);

    /**
     * Inserts an identified session into a free slot or atomically replaces its
     * terminal record on confirmed PID reuse.
     *
     * @throws SessionException with code SLOT_OCCUPIED if the slot is occupied otherwise
     */
    void create(@NotNull @Valid Session session);

    /**
     * Updates the active record with the ID carried by updated.
     *
     * @throws SessionException with code CONCURRENT_CHANGE if the record is
     * absent, the terminal shell identity conflicts, or the link target is inactive
     */
    void update(@NotNull @Valid Session updated);

    /**
     * Atomically updates active sessions by the IDs carried by updated.
     * Every entry must have a distinct slot ID.
     *
     * @throws SessionException with code CONCURRENT_CHANGE if any record is absent,
     * a terminal shell identity conflicts, or a link target is inactive.
     * The batch applies no updates on failure.
     */
    void replace(@NotEmpty List<@NotNull @Valid Session> updated);

    /**
     * Removes the matching slot and clears its links atomically.
     *
     * @throws SessionException with code SESSION_INACTIVE if the slot is absent after cleanup
     */
    void remove(@NotNull @Valid SessionSlot slot);

    /**
     * Deletes the named slot and clears its links atomically.
     *
     * @throws SessionException with code NAMED_SESSION_MISSING if the name is absent after cleanup
     */
    void deleteNamed(@NotNull @Pattern(regexp = SessionSlot.NAME_FORMAT) String name);

    /** Removes sessions referencing a deleted workspace and clears their links atomically. */
    void removeByWorkspace(@NotNull UUID workspaceId);

    /** Removes the user's terminal slots on logout. */
    void removeTerminalByUser(@NotNull UUID userId);
}
