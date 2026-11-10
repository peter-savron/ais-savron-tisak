package si.fri.ais.user.core.models.commmands;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.SuperBuilder;

@Getter
@SuperBuilder
public class UserPasswordUpdate {
    @NotNull private String name;
    @NotEmpty private char[] password;
}
