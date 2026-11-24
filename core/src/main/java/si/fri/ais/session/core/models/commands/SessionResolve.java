package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.session.core.models.SessionSlot;

/** Resolves preferences from the selected slot, following its link if present. */
@Getter
@SuperBuilder
public class SessionResolve {
    @NotNull @Valid private final SessionSlot slot;
}
