package si.fri.ais.session.core.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Session preferences and lifecycle timestamps. A null slot represents a session
 * without a stored identifier. A null workspace leaves workspace selection to callers.
 * A null usesSession means the session supplies its own preferences.
 */
@Getter
@Setter
@SuperBuilder
public class Session {
    @Valid private final SessionSlot slot;
    private UUID workspaceId;
    @NotNull private UUID userId;
    @Pattern(regexp = SessionSlot.SHARED_ID_FORMAT) private String usesSession;
    @NotNull private Instant lastAccessedAt;
    @NotNull private Instant expiresAt;

    public String getId() {
        return slot == null ? null : slot.getId();
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
