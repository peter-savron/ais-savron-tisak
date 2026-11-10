package si.fri.ais.common.core.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
public class UnnamedObject {
    @NotNull private UUID id;
}
