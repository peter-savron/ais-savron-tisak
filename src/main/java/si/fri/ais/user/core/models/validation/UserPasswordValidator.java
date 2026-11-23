package si.fri.ais.user.core.models.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.CharBuffer;
import java.util.regex.Pattern;

import static si.fri.ais.common.core.utils.MainApplicationValues.MAX_PASSWORD_LENGTH;
import static si.fri.ais.common.core.utils.MainApplicationValues.MIN_PASSWORD_LENGTH;

public class UserPasswordValidator implements ConstraintValidator<ValidUserPassword, char[]> {

    private static final Pattern PASSWORD_FORMAT = Pattern.compile("[A-Za-z0-9\\p{Punct}]+");

    @Override
    public boolean isValid(char[] password, ConstraintValidatorContext context) {
        return password == null || (password.length >= MIN_PASSWORD_LENGTH
                && password.length <= MAX_PASSWORD_LENGTH
                && PASSWORD_FORMAT.matcher(CharBuffer.wrap(password)).matches());
    }
}
