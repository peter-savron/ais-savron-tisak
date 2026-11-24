package si.fri.ais.session.core.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** The refreshed parent session and its refreshed target, when a link was followed. */
public record ResolvedSession(@NotNull @Valid Session session, @Valid Session linkedSession) {
    public Session effectiveSession() {
        return linkedSession == null ? session : linkedSession;
    }
}
