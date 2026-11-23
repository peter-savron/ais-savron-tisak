package si.fri.ais.user.core.models.commands;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.user.core.models.validation.ValidUserPassword;

@Getter
@SuperBuilder
public class UserCreate {
    @NotEmpty
    private String name;

    @NotNull
    @ValidUserPassword
    private char[] password;
}
