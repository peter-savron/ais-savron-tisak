package si.fri.ais.user.core.models.commands;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.common.core.utils.MainApplicationValues;

@Getter
@SuperBuilder
public class UserCreate {
    @NotEmpty
    private String name;

    @NotNull
    @Size(min = MainApplicationValues.MIN_PASSWORD_LENGTH, max = MainApplicationValues.MAX_PASSWORD_LENGTH)
    private char[] password;
}
