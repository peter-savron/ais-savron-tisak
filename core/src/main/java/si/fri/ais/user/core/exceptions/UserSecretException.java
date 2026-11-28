package si.fri.ais.user.core.exceptions;

import lombok.Getter;

/** A secret operation failed without exposing its plaintext or key material. */
@Getter
public class UserSecretException extends RuntimeException {
    public enum Code {
        OWNER_NOT_FOUND,
        USER_LOGGED_OUT,
        KEY_UNAVAILABLE,
        INVALID_PASSWORD,
        SECRET_NOT_FOUND,
        NAME_NOT_UNIQUE,
        ENCRYPTION_FAILED,
        SECRET_INTEGRITY_FAILURE,
        UNSUPPORTED_FORMAT,
        STORAGE_FAILURE
    }

    private final Code code;

    public UserSecretException(Code code) {
        super(code.name());
        this.code = code;
    }

    public UserSecretException(Code code, Throwable cause) {
        super(code.name(), cause);
        this.code = code;
    }
}
