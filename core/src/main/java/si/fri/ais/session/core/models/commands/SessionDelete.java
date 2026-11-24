package si.fri.ais.session.core.models.commands;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import si.fri.ais.session.core.models.SessionSlot;

public record SessionDelete(@NotNull @Pattern(regexp = SessionSlot.NAME_FORMAT) String name) {
}
