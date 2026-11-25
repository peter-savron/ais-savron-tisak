package si.fri.ais.workspace.core.exceptions;

import lombok.Getter;

@Getter
public class WorkspaceException extends IllegalArgumentException {
    public enum Code {
        OWNER_NOT_FOUND,
        NAME_NOT_UNIQUE,
        WORKSPACE_NOT_FOUND,
        DELETION_BLOCKED
    }

    private final Code code;

    public WorkspaceException(Code code) {
        super(code.name());
        this.code = code;
    }
}
