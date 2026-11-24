package si.fri.ais.common.core.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.SuperBuilder;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@Getter
@SuperBuilder
@FieldNameConstants
public class UnnamedObject {
    @NotNull private UUID id;
}
