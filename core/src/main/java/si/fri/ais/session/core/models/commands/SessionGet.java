package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.session.core.models.SessionSlot;

/** Resolves the selected slot and its linked session, if present. */
@Getter
@SuperBuilder
public class SessionGet {
    @NotNull @Valid private final SessionSlot slot;
}
