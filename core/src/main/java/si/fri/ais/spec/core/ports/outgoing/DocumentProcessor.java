package si.fri.ais.spec.core.ports.outgoing;

import si.fri.ais.spec.core.exceptions.DocumentProcessingException;
import si.fri.ais.spec.core.models.OriginalSpec;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public interface DocumentProcessor {
    /** Returns the normalized text format. Leaves the caller's destination open. */
    String process(OriginalSpec original, Path file, OutputStream destination) throws DocumentProcessingException;

    interface FileTypeDeterminer {
        String detect(Path file) throws IOException;
    }
    interface Converter {
        boolean accepts(String mimeType);
        int priority();
        /** Write normalized UTF-8 text and return its MIME type. */
        String convert(OriginalSpec original, Path file, OutputStream destination) throws IOException, DocumentProcessingException;
    }
}
