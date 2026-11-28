package si.fri.ais.user.core.ports.outgoing;

import si.fri.ais.user.core.models.StoredUserSecret;
import si.fri.ais.user.core.models.UserKey;

/**
 * Cryptographic operations on encrypted secret models. This port performs no
 * authorization or persistence. Failures use {@code UserSecretException}; keys
 * belong to the caller and remain open after an operation.
 */
public interface UserSecretCipher {
    /** Replaces the envelope in place using a fresh data key and IVs; does not modify plaintext. */
    void encrypt(StoredUserSecret secret, byte[] plaintext, UserKey key);

    /** Returns authenticated plaintext; the caller owns and must clear the returned array. */
    byte[] decrypt(StoredUserSecret secret, UserKey key);

    /** Rewraps the data key in place without decrypting or changing the encrypted value. */
    void rewrap(StoredUserSecret secret, UserKey oldKey, UserKey newKey);
}
