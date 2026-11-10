package si.fri.ais.user.core.models.commmands;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.SuperBuilder;

@Getter
@SuperBuilder
public class UserLogin {
    @NotNull private String name;
    @NotNull private char[] password;
}
