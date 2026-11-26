package si.fri.ais.spec.core.exceptions;

/** A checked failure while detecting, converting or writing a document. */
public class DocumentProcessingException extends Exception {
    public DocumentProcessingException(String message) {
        super(message);
    }

    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
