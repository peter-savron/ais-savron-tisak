package si.fri.ais.spec.core.exceptions;

/** A registration already reserves the requested name in this workspace. */
public class SpecSourceNameConflictException extends SpecSourceException {
    public SpecSourceNameConflictException(String message) {
        super(Code.NAME_CONFLICT, message);
    }
}
