package si.fri.ais.spec.core.exceptions;

import lombok.Getter;

/** A source operation failure with a stable reason code. */
@Getter
public class SpecSourceException extends RuntimeException {
    public enum Code {
        INVALID_CONFIGURATION, UNSUPPORTED_SOURCE_TYPE, CONFLICTING_SUBFOLDER_OPTIONS,
        SOURCE_BUSY, NAME_CONFLICT, WORKSPACE_UNAVAILABLE, USER_UNAVAILABLE,
        SOURCE_UNAVAILABLE,
        AMBIGUOUS_PROVIDER, DISCOVERY_FAILED
    }

    private final Code code;

    public SpecSourceException(Code code, String message) {
        this(code, message, null);
    }

    public SpecSourceException(Code code, String message, Throwable cause) {
        super(code.name() + ": " + message, cause);
        this.code = code;
    }
}
