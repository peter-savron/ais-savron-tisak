package si.fri.ais.user.api.cli.actions;

import jakarta.enterprise.context.ApplicationScoped;
import org.jline.reader.LineReader;
import si.fri.ais.common.api.cli.prompts.LinePrompt;

import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class UserNameResolver {

    private static final Pattern USER_NAME = Pattern.compile("[A-Za-z][A-Za-z0-9_-]{0,63}");
    private static final String INVALID_USER_NAME =
            "User name must be at most 64 characters, start with an ASCII letter, "
                    + "and contain only ASCII letters, digits, underscores, or hyphens.";

    private final LineReader lineReader;

    public UserNameResolver(LineReader lineReader) {
        this.lineReader = lineReader;
    }

    String readUserName(String name) {
        if (name != null) {
            validateName(name);
            return name;
        }

        while (true) {
            String input = LinePrompt.read(lineReader, "User name: ");
            if (USER_NAME.matcher(input).matches()) {
                return input;
            }
            lineReader.printAbove(INVALID_USER_NAME);
        }
    }

    String resolveUserName(String name) {
        String resolvedName = Optional.ofNullable(name)
                .or(() -> Optional.ofNullable(System.getenv("BISTRA_SESSION_USER"))
                        .filter(sessionUser -> !sessionUser.isBlank()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Specify --name or set BISTRA_SESSION_USER for this session."));
        validateName(resolvedName);
        return resolvedName;
    }

    private static void validateName(String name) {
        if (!USER_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(INVALID_USER_NAME);
        }
    }
}
