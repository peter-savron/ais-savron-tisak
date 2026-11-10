package si.fri.ais.user.core.models;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.SuperBuilder;
import si.fri.ais.common.core.models.NamedObject;

@Getter
@Setter
@SuperBuilder
public class User extends NamedObject {
    @NotNull private String passwordHash;
    @NotEmpty private byte[] keySalt;
}
