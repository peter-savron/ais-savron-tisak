package si.fri.ais.user.api.cli.exceptions;

import lombok.Getter;

/** CLI failures whose messages never contain secret values or credentials. */
@Getter
public class UserSecretCliException extends RuntimeException {
    public enum Code {
        INVALID_USER_NAME,
        INVALID_SECRET_NAME,
        USER_NOT_FOUND,
        LOGIN_REQUIRED,
        USER_REQUIRED,
        NAME_REQUIRED,
        VALUE_FILE_REQUIRED,
        BINARY_FILE_REQUIRED,
        VALUE_FILE_UNREADABLE,
        INVALID_VALUE,
        INVALID_TEXT_ENCODING,
        PASSWORD_REQUIRED,
        INVALID_PASSWORD,
        SECRET_NOT_FOUND,
        OUTPUT_FAILURE
    }

    private final Code code;

    public UserSecretCliException(Code code) {
        this(code, null);
    }

    public UserSecretCliException(Code code, Throwable cause) {
        super(code.name(), cause);
        this.code = code;
    }
}
