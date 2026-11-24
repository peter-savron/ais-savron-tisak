package si.fri.ais.session.core.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** The refreshed parent session and its refreshed target, when a link was followed. */
@Getter
@Setter
@SuperBuilder
public class ResolvedSession {
    @NotNull @Valid private Session session;
    @Valid private Session linkedSession;

    public Session getEffectiveSession() {
        return linkedSession == null ? session : linkedSession;
    }
}
