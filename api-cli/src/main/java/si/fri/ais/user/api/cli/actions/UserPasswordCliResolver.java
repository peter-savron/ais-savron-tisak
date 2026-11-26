package si.fri.ais.user.api.cli.actions;

import jakarta.enterprise.context.ApplicationScoped;
import org.jline.reader.LineReader;
import si.fri.ais.common.api.cli.prompts.PasswordPrompt;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.regex.Pattern;

import static si.fri.ais.common.core.utils.MainApplicationValues.MAX_PASSWORD_LENGTH;
import static si.fri.ais.common.core.utils.MainApplicationValues.MIN_PASSWORD_LENGTH;

@ApplicationScoped
public class UserPasswordCliResolver {

    private static final Pattern PASSWORD_FORMAT = Pattern.compile("[A-Za-z0-9\\p{Punct}]+");
    private static final String PASSWORD_FORMAT_MESSAGE =
            "Use only ASCII letters, digits, or punctuation.";

    private final LineReader lineReader;
    private final PasswordPrompt passwordPrompt;

    public UserPasswordCliResolver(LineReader lineReader, PasswordPrompt passwordPrompt) {
        this.lineReader = lineReader;
        this.passwordPrompt = passwordPrompt;
    }

    char[] readPassword(char[] suppliedPassword) {
        return readPassword(suppliedPassword, "Password: ");
    }

    public char[] readPassword(char[] suppliedPassword, String message) {
        if (suppliedPassword != null) {
            validatePassword(suppliedPassword);
            return suppliedPassword;
        }

        lineReader.printAbove("Enter a password with %d to %d characters. %s Input is masked."
                .formatted(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH, PASSWORD_FORMAT_MESSAGE));
        while (true) {
            char[] password = passwordPrompt.read(message);
            try {
                validatePassword(password);
                return password;
            } catch (IllegalArgumentException exception) {
                Arrays.fill(password, '\0');
                lineReader.printAbove(exception.getMessage());
            }
        }
    }

    private static void validatePassword(char[] password) {
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must contain at least %d characters."
                    .formatted(MIN_PASSWORD_LENGTH));
        }
        if (password.length > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must contain at most %d characters."
                    .formatted(MAX_PASSWORD_LENGTH));
        }
        if (!PASSWORD_FORMAT.matcher(CharBuffer.wrap(password)).matches()) {
            throw new IllegalArgumentException("Invalid password format. " + PASSWORD_FORMAT_MESSAGE);
        }
    }
}
