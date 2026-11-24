package si.fri.ais.session.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.session.core.models.SessionSlot;

@Getter
@SuperBuilder
public class SessionStop {
    @NotNull @Valid private final SessionSlot slot;
}
