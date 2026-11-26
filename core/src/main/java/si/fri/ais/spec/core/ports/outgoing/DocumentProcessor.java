package si.fri.ais.spec.core.ports.outgoing;

import si.fri.ais.spec.core.exceptions.DocumentProcessingException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public interface DocumentProcessor {
    /** Writes normalized text and returns its output MIME type. Leaves the caller's destination open. */
    String process(Path file, OutputStream destination) throws DocumentProcessingException;

    interface FileTypeDeterminer {
        String detect(Path file) throws IOException;
    }
    interface Converter {
        boolean accepts(String mimeType);
        int priority();
        /** Write normalized UTF-8 text and return its MIME type. */
        String convert(Path file, OutputStream destination) throws IOException, DocumentProcessingException;
    }
}
