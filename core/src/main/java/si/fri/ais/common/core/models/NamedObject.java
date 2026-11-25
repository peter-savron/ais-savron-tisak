package si.fri.ais.common.core.models;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.Getter;
import lombok.SuperBuilder;
import lombok.experimental.FieldNameConstants;

@Getter
@SuperBuilder
@FieldNameConstants
public class NamedObject extends UnnamedObject {
    public static final String NAME_FORMAT = "[A-Za-z][A-Za-z0-9_-]{0,63}";

    @NotNull @Setter(AccessLevel.PROTECTED) private String name;
}
