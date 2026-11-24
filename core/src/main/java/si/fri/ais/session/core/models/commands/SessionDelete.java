package si.fri.ais.session.core.models.commands;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.session.core.models.SessionSlot;

@Getter
@SuperBuilder
public class SessionDelete {
    @NotNull @Pattern(regexp = SessionSlot.NAME_FORMAT) private final String name;
}
