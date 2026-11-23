package si.fri.ais.user.core.models.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static si.fri.ais.common.core.utils.MainApplicationValues.MAX_PASSWORD_LENGTH;
import static si.fri.ais.common.core.utils.MainApplicationValues.MIN_PASSWORD_LENGTH;

@Constraint(validatedBy = UserPasswordValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserPassword {

    String message() default "must contain " + MIN_PASSWORD_LENGTH + " to " + MAX_PASSWORD_LENGTH
            + " characters using only ASCII letters, digits, or punctuation";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
