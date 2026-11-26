package si.fri.ais.spec.core.exceptions;

import lombok.Getter;

/** Checked failure retrieving an item or releasing its read resources. */
@Getter
public class SpecSourceReadException extends Exception {
    public enum Code { READ_FAILED, CLEANUP_FAILED, VERSION_CHANGED, MISSING, EXCLUDED, ACCESS_DENIED, UNRESOLVED_ITEM }

    private final Code code;

    public SpecSourceReadException(Code code, String message, Throwable cause) {
        super(code.name() + ": " + message, cause);
        this.code = code;
    }
}
