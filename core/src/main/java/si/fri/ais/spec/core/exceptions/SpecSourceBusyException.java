package si.fri.ais.spec.core.exceptions;

/** A source mutation conflicts with an ongoing source operation. */
public class SpecSourceBusyException extends SpecSourceException {
    public SpecSourceBusyException(String message) {
        super(Code.SOURCE_BUSY, message);
    }
}
