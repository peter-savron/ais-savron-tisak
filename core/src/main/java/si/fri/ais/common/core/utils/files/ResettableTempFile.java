package si.fri.ais.common.core.utils.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResettableTempFile implements ResettableFile {

    private final Path tempPath;

    private ResettableTempFile(final Path tempPath) {
        this.tempPath = tempPath;
    }

    public static ResettableTempFile create(final String prefix, final String suffix) throws IOException {
        return new ResettableTempFile(Files.createTempFile(prefix, suffix));
    }

    public static ResettableTempFile createIn(
            final Path directory, final String prefix, final String suffix) throws IOException {
        return new ResettableTempFile(Files.createTempFile(directory, prefix, suffix));
    }

    @Override
    public InputStream openStream() throws IOException {
        return Files.newInputStream(tempPath);
    }

    @Override
    public Path path() {
        return tempPath;
    }

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(tempPath);
    }
}