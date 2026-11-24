package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.session.core.models.SessionSlot;

/** Reads the selected slot directly. */
public record SessionGet(@NotNull @Valid SessionSlot slot) {
}
