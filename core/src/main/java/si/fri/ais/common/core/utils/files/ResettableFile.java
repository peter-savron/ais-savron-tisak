package si.fri.ais.common.core.utils.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface ResettableFile extends AutoCloseable {

    /**
     * Opens a new InputStream positioned at the beginning of the file.
     * Callers are responsible for closing each returned stream.
     */
    InputStream openStream() throws IOException;

    /**
     * Returns the underlying file path.
     */
    Path path();

    /**
     * Releases or deletes the underlying file resource if applicable.
     */
    @Override
    void close() throws IOException;
}