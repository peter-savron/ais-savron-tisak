package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.session.core.models.SessionSlot;

/** Resolves preferences from the selected slot, following its link if present. */
public record SessionResolve(@NotNull @Valid SessionSlot slot) {
}
