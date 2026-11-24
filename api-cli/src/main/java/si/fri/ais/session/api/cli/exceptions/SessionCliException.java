package si.fri.ais.session.api.cli.exceptions;

import lombok.Getter;

@Getter
public class SessionCliException extends IllegalStateException {
    public enum Code {
        SHELL_UNAVAILABLE,
        INVALID_SESSION_NAME,
        NO_NAMED_SESSIONS,
        USER_REQUIRED,
        USER_OR_WORKSPACE_REQUIRED,
        SESSION_NAME_REQUIRED,
        SESSION_TARGET_REQUIRED,
        WORKSPACE_LOOKUP_UNAVAILABLE,
        INVALID_USER_NAME,
        USER_NOT_FOUND,
        SESSION_INACTIVE,
        PREFERENCES_UNCHANGED
    }

    private final Code code;
    private final String reference;

    public SessionCliException(Code code) {
        this(code, null);
    }

    public SessionCliException(Code code, String reference) {
        super(code.name());
        this.code = code;
        this.reference = reference;
    }
}
