package si.fri.ais.user.api.cli.actions;

import jakarta.enterprise.context.ApplicationScoped;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import si.fri.ais.common.api.cli.prompts.LinePrompt;
import si.fri.ais.common.api.cli.prompts.PasswordPrompt;
import si.fri.ais.common.core.models.NamedObject;
import si.fri.ais.session.api.cli.actions.SessionCliResolver;
import si.fri.ais.session.core.models.ResolvedSession;
import si.fri.ais.session.core.models.commands.SessionResolve;
import si.fri.ais.session.core.ports.outgoing.SessionService;
import si.fri.ais.user.api.cli.exceptions.UserSecretCliException;
import si.fri.ais.user.api.cli.exceptions.UserSecretCliExceptionMapper;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.UserSecret;
import si.fri.ais.user.core.models.UserSecretContext;
import si.fri.ais.user.core.ports.outgoing.UserService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static si.fri.ais.user.api.cli.exceptions.UserSecretCliException.Code.*;

/** Shared owner selection, names, and secret input for the secret CLI actions. */
@ApplicationScoped
public class UserSecretCliResolver {
    private static final Pattern NAME = Pattern.compile(NamedObject.NAME_FORMAT);
    private static final Pattern INTEGER = Pattern.compile("[+-]?[0-9]+");
    private static final Pattern DECIMAL = Pattern.compile("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?");

    private final LineReader reader;
    private final PasswordPrompt prompt;
    private final UserPasswordCliResolver passwords;
    private final UserService users;
    private final SessionService sessions;
    private final SessionCliResolver sessionSlots;
    private final UserSecretCliExceptionMapper messages;

    public UserSecretCliResolver(LineReader reader, PasswordPrompt prompt, UserPasswordCliResolver passwords,
                                  UserService users, SessionService sessions, SessionCliResolver sessionSlots,
                                  UserSecretCliExceptionMapper messages) {
        this.reader = reader;
        this.prompt = prompt;
        this.passwords = passwords;
        this.users = users;
        this.sessions = sessions;
        this.sessionSlots = sessionSlots;
        this.messages = messages;
    }

    User user(String suppliedName) {
        String name = Optional.ofNullable(suppliedName)
                .or(() -> Optional.ofNullable(System.getenv("BISTRA_SESSION_USER")).filter(value -> !value.isBlank()))
                .orElse(null);
        if (name != null) {
            return requireUser(name);
        }
        Optional<ResolvedSession> session = sessions.resolveDefaults(
                        SessionResolve.builder().slot(sessionSlots.slot(false, null)).build())
                .or(() -> sessions.resolveDefaults(SessionResolve.builder().slot(sessionSlots.slot(true, null)).build()));
        if (session.isPresent()) {
            return requireLoggedIn(users.getUser(session.get().getEffectiveSession().getUserId())
                    .orElseThrow(() -> new UserSecretCliException(USER_NOT_FOUND)));
        }
        if (!interactive()) {
            throw new UserSecretCliException(USER_REQUIRED);
        }
        while (true) {
            try {
                return requireUser(LinePrompt.read(reader, "User name: "));
            } catch (UserSecretCliException failure) {
                reader.printAbove(messages.messageFor(failure));
            }
        }
    }

    String name(String suppliedName) {
        if (suppliedName != null) {
            if (!NAME.matcher(suppliedName).matches()) {
                throw new UserSecretCliException(INVALID_SECRET_NAME);
            }
            return suppliedName;
        }
        if (!interactive()) {
            throw new UserSecretCliException(NAME_REQUIRED);
        }
        while (true) {
            String value = LinePrompt.read(reader, "Secret name: ");
            if (NAME.matcher(value).matches()) {
                return value;
            }
            reader.printAbove(UserSecretCliExceptionMapper.NAME_RULES);
        }
    }

    UserSecretContext context(UUID userId, String name) {
        return UserSecretContext.builder().userId(userId).secretName(name).build();
    }

    byte[] value(Path file, UserSecret.Type type) {
        if (file != null) {
            try {
                byte[] value = Files.readAllBytes(file);
                try {
                    validateValue(value, type);
                    return value;
                } catch (RuntimeException failure) {
                    Arrays.fill(value, (byte) 0);
                    throw failure;
                }
            } catch (IOException failure) {
                throw new UserSecretCliException(VALUE_FILE_UNREADABLE, failure);
            }
        }
        if (type == UserSecret.Type.BYTES) {
            throw new UserSecretCliException(BINARY_FILE_REQUIRED);
        }
        if (!interactive()) {
            throw new UserSecretCliException(VALUE_FILE_REQUIRED);
        }
        reader.printAbove("Secret input is masked. Enter submits; Ctrl+C cancels.");
        while (true) {
            char[] characters = prompt.read("Secret value: ");
            ByteBuffer encoded = null;
            byte[] value = null;
            try {
                encoded = StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(characters));
                value = new byte[encoded.remaining()];
                encoded.get(value);
                validateValue(value, type);
                return value;
            } catch (CharacterCodingException failure) {
                reader.printAbove(messages.messageFor(new UserSecretCliException(INVALID_TEXT_ENCODING)));
            } catch (UserSecretCliException failure) {
                Optional.ofNullable(value).ifPresent(bytes -> Arrays.fill(bytes, (byte) 0));
                reader.printAbove(messages.messageFor(failure));
            } finally {
                Arrays.fill(characters, '\0');
                if (encoded != null && encoded.hasArray()) {
                    Arrays.fill(encoded.array(), (byte) 0);
                }
            }
        }
    }

    char[] password(char[] suppliedPassword) {
        if (suppliedPassword == null && !interactive()) {
            throw new UserSecretCliException(PASSWORD_REQUIRED);
        }
        try {
            return passwords.readPassword(suppliedPassword, "Password to reveal secret: ");
        } catch (IllegalArgumentException failure) {
            throw new UserSecretCliException(INVALID_PASSWORD, failure);
        }
    }

    CharBuffer text(byte[] value) {
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(value));
        } catch (CharacterCodingException failure) {
            throw new UserSecretCliException(INVALID_TEXT_ENCODING, failure);
        }
    }

    private void validateValue(byte[] value, UserSecret.Type type) {
        if (type == UserSecret.Type.BYTES) {
            return;
        }
        CharBuffer text = text(value);
        try {
            if ((type == UserSecret.Type.INTEGER && !INTEGER.matcher(text).matches())
                    || (type == UserSecret.Type.DECIMAL && !DECIMAL.matcher(text).matches())) {
                throw new UserSecretCliException(INVALID_VALUE);
            }
        } finally {
            if (text.hasArray()) {
                Arrays.fill(text.array(), '\0');
            }
        }
    }

    private User requireUser(String name) {
        if (!NAME.matcher(name).matches()) {
            throw new UserSecretCliException(INVALID_USER_NAME);
        }
        return requireLoggedIn(users.getUser(name).orElseThrow(() -> new UserSecretCliException(USER_NOT_FOUND)));
    }

    private User requireLoggedIn(User user) {
        if (!user.isLogged()) {
            throw new UserSecretCliException(LOGIN_REQUIRED);
        }
        return user;
    }

    private boolean interactive() {
        String type = reader.getTerminal().getType();
        return !Terminal.TYPE_DUMB.equals(type) && !Terminal.TYPE_DUMB_COLOR.equals(type);
    }
}
