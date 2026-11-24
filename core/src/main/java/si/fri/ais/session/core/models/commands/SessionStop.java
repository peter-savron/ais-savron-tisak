package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.session.core.models.SessionSlot;

public record SessionStop(@NotNull @Valid SessionSlot slot) {
}
