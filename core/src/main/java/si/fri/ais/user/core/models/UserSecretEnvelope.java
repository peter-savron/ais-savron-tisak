package si.fri.ais.user.core.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Versioned authenticated ciphertext and its encrypted data encryption key. */
@Getter
@Setter
@SuperBuilder
public class UserSecretEnvelope {
    /** Cryptographic format version; version 1 uses AES-256-GCM. */
    private int version;
    /** Encrypted value including its 128-bit authentication tag. */
    @NotNull @Size(min = 16) private byte[] ciphertext;
    /** 96-bit IV used to encrypt the value. */
    @NotNull @Size(min = 12, max = 12) private byte[] iv;
    /** Encrypted 256-bit data key including its 128-bit authentication tag. */
    @NotNull @Size(min = 48, max = 48) private byte[] wrappedKey;
    /** Independent 96-bit IV used to wrap the data key. */
    @NotNull @Size(min = 12, max = 12) private byte[] keyIv;
}
