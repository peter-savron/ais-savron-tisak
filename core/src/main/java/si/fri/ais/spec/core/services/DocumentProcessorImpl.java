package si.fri.ais.spec.core.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;
import si.fri.ais.spec.core.exceptions.DocumentProcessingException;
import si.fri.ais.spec.core.models.OriginalSpec;
import si.fri.ais.spec.core.ports.outgoing.DocumentProcessor;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Comparator;

@ApplicationScoped
@RequiredArgsConstructor
public class DocumentProcessorImpl implements DocumentProcessor {
    private final FileTypeDeterminer determiner;
    private final Instance<Converter> converters;

    @Override
    public String process(OriginalSpec original, Path file, OutputStream destination)
            throws DocumentProcessingException {
        try {
            String mime = determiner.detect(file);
            var converter = converters.stream().filter(c -> c.accepts(mime))
                    .max(Comparator.comparingInt(Converter::priority))
                    .orElseThrow(() -> new DocumentProcessingException("Unsupported format " + mime));
            var buffered = new BufferedOutputStream(destination);
            String format = converter.convert(original, file, buffered);
            buffered.flush();
            return format;
        } catch (IOException e) {
            throw new DocumentProcessingException("I/O failed while processing document", e);
        }
    }
}
