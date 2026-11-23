package si.fri.ais.user.core.exceptions;

import lombok.Getter;

@Getter
public class UserException extends IllegalArgumentException {

    public enum Code {
        USER_CREATE_NAME_NOT_UNIQUE
    }

    private final Code code;

    public UserException(Code code) {
        super(code.name());
        this.code = code;
    }
}
