package si.fri.ais.common.core.utils.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ResettablePermFile implements ResettableFile {

    private final Path filePath;

    public ResettablePermFile(final Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
    }

    @Override
    public InputStream openStream() throws IOException {
        return Files.newInputStream(filePath);
    }

    @Override
    public Path path() {
        return filePath;
    }

    @Override
    public void close() {
        // Intentional no-op: permanent files are not deleted on close
    }
}