package si.fri.ais.session.core.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.UUID;

/**
 * Session preferences and lifecycle timestamps. A null slot represents a session
 * without a stored identifier. A null workspace leaves workspace selection to callers.
 * A null usesSession means the session supplies its own preferences.
 */
public record Session(
        @Valid SessionSlot slot,
        UUID workspaceId,
        @NotNull UUID userId,
        @Pattern(regexp = SessionSlot.SHARED_ID_FORMAT) String usesSession,
        @NotNull Instant lastAccessedAt,
        @NotNull Instant expiresAt
) {
    public String id() {
        return slot == null ? null : slot.id();
    }

    @AssertTrue(message = "Only terminal sessions can use a shared session")
    public boolean isLinkValid() {
        return usesSession == null || (slot != null && slot.isTerminal());
    }

    @AssertTrue(message = "Expiry must be after last access")
    public boolean isExpiryValid() {
        return lastAccessedAt == null || expiresAt == null || expiresAt.isAfter(lastAccessedAt);
    }
}
