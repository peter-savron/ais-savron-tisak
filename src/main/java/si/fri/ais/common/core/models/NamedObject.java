package si.fri.ais.common.core.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.SuperBuilder;

@Getter
@SuperBuilder
public class NamedObject extends UnnamedObject {
    @NotNull private String name;
}
