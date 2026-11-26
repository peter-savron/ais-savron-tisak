package si.fri.ais.workspace.api.cli.exceptions;

import lombok.Getter;

@Getter
public class WorkspaceCliException extends RuntimeException {
    public enum Code {
        INVALID_NAME,
        USER_NOT_FOUND,
        LOGIN_REQUIRED,
        NAME_REQUIRED_FOR_CREATE,
        NAME_REQUIRED_FOR_SELECTION,
        NO_WORKSPACES,
        EDIT_REQUIRED,
        DESCRIPTION_TOO_LONG,
        PASSWORD_REQUIRED,
        INVALID_PASSWORD_OPTION
    }

    private final Code code;
    private final String reference;

    public WorkspaceCliException(Code code) {
        this(code, null);
    }

    public WorkspaceCliException(Code code, String reference) {
        this(code, reference, null);
    }

    public WorkspaceCliException(Code code, String reference, Throwable cause) {
        super(code.name(), cause);
        this.code = code;
        this.reference = reference;
    }
}
