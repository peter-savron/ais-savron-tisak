package si.fri.ais.session.core.exceptions;

import lombok.Getter;

@Getter
public class SessionException extends IllegalStateException {
    public enum Code {
        SLOT_OCCUPIED,
        SESSION_INACTIVE,
        LINKED_SESSION_INACTIVE,
        NAMED_SESSION_MISSING,
        USER_MISSING,
        LOGIN_REQUIRED,
        WORKSPACE_UNAVAILABLE,
        PREFERENCES_UNCHANGED,
        CONCURRENT_CHANGE
    }

    private final Code code;
    private final String reference;

    public SessionException(Code code, String reference) {
        super(code.name() + ": " + reference);
        this.code = code;
        this.reference = reference;
    }
}
