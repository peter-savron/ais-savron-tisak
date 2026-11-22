package si.fri.ais.user.core.models.commands;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import si.fri.ais.common.core.utils.MainApplicationValues;

@Getter
@SuperBuilder
public class UserCreate {
    @NotEmpty
    private String name;

    @Length(min = MainApplicationValues.MIN_PASSWORD_LENGTH + 1,  max = MainApplicationValues.MAX_PASSWORD_LENGTH + 1) // Include \0 sign at end.
    private char[] password;
}
