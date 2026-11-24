package si.fri.ais.user.core.models;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public class UserKey implements AutoCloseable {

    private UserKey(byte[] key) {
        this.key = Arrays.copyOf(key, key.length);
        Arrays.fill(key, (byte) 0);
    }

    /**
     * Initializes key content, zeroes input array.
     *
     * @param key key bytes, will be zeroed at the end of the operation.
     * @return the created key instance.
     */
    public static UserKey init(byte[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("Key material cannot be null or empty.");
        }
        return new UserKey(key);
    }

    private final byte[] key;
    private boolean closed = false;

    public <T> T useKey(Function<byte[], T> function) {
        checkNotClosed();
        return function.apply(key);
    }

    public void useKey(Consumer<byte[]> consumer) {
        checkNotClosed();
        consumer.accept(key);
    }

    @Override
    public void close() {
        if (!closed) {
            Arrays.fill(key, (byte) 0);
            closed = true;
        }
    }

    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("UserKey was zeroed.");
        }
    }
}
