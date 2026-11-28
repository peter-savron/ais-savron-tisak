package si.fri.ais.user.core.services;

import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.user.core.exceptions.UserSecretException;
import si.fri.ais.user.core.models.StoredUserSecret;
import si.fri.ais.user.core.models.UserKey;
import si.fri.ais.user.core.models.UserSecretEnvelope;
import si.fri.ais.user.core.ports.outgoing.UserSecretCipher;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import static si.fri.ais.user.core.exceptions.UserSecretException.Code.ENCRYPTION_FAILED;
import static si.fri.ais.user.core.exceptions.UserSecretException.Code.SECRET_INTEGRITY_FAILURE;
import static si.fri.ais.user.core.exceptions.UserSecretException.Code.UNSUPPORTED_FORMAT;

/** AES-256-GCM envelopes binding both ciphertexts to their owner, ID, name and type. */
@ApplicationScoped
public class UserSecretCipherImpl implements UserSecretCipher {
    private static final int VERSION = 1;
    private final SecureRandom random = new SecureRandom();

    /** Replaces the record's envelope using a fresh data key and independent random IVs. */
    @Override
    public void encrypt(StoredUserSecret secret, byte[] plaintext, UserKey key) {
        byte[] dataKey = new byte[32];
        random.nextBytes(dataKey);
        try (UserKey dek = UserKey.init(dataKey)) {
            byte[] iv = new byte[12];
            byte[] keyIv = new byte[12];
            random.nextBytes(iv);
            random.nextBytes(keyIv);
            secret.setEnvelope(UserSecretEnvelope.builder()
                    .version(VERSION)
                    .ciphertext(crypt(Cipher.ENCRYPT_MODE, dek, iv, aad(secret, "value"), plaintext))
                    .iv(iv)
                    .wrappedKey(dek.useKey(bytes -> {
                        return crypt(Cipher.ENCRYPT_MODE, key, keyIv, aad(secret, "key"), bytes);
                    }))
                    .keyIv(keyIv)
                    .build());
        }
    }

    /** Returns newly allocated plaintext only after both authentication tags are verified. */
    @Override
    public byte[] decrypt(StoredUserSecret secret, UserKey key) {
        requireEnvelope(secret.getEnvelope());
        try (UserKey dek = UserKey.init(crypt(Cipher.DECRYPT_MODE, key,
                secret.getEnvelope().getKeyIv(), aad(secret, "key"), secret.getEnvelope().getWrappedKey()))) {
            return crypt(Cipher.DECRYPT_MODE, dek, secret.getEnvelope().getIv(),
                    aad(secret, "value"), secret.getEnvelope().getCiphertext());
        }
    }

    /** Rewraps the data key in place; the value's ciphertext and IV remain untouched. */
    @Override
    public void rewrap(StoredUserSecret secret, UserKey oldKey, UserKey newKey) {
        requireEnvelope(secret.getEnvelope());
        try (UserKey dek = UserKey.init(crypt(Cipher.DECRYPT_MODE, oldKey,
                secret.getEnvelope().getKeyIv(), aad(secret, "key"), secret.getEnvelope().getWrappedKey()))) {
            byte[] keyIv = new byte[12];
            random.nextBytes(keyIv);
            secret.getEnvelope().setWrappedKey(dek.useKey(bytes -> {
                return crypt(Cipher.ENCRYPT_MODE, newKey, keyIv, aad(secret, "key"), bytes);
            }));
            secret.getEnvelope().setKeyIv(keyIv);
        }
    }

    private byte[] crypt(int mode, UserKey key, byte[] iv, byte[] aad, byte[] input) {
        return key.useKey(bytes -> {
            if (bytes.length != 32) {
                throw new UserSecretException(mode == Cipher.ENCRYPT_MODE ? ENCRYPTION_FAILED : SECRET_INTEGRITY_FAILURE);
            }
            try {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(mode, new SecretKeySpec(bytes, "AES"), new GCMParameterSpec(128, iv));
                cipher.updateAAD(aad);
                return cipher.doFinal(input);
            } catch (GeneralSecurityException failure) {
                throw new UserSecretException(mode == Cipher.ENCRYPT_MODE
                        ? ENCRYPTION_FAILED : SECRET_INTEGRITY_FAILURE, failure);
            }
        });
    }

    private byte[] aad(StoredUserSecret secret, String purpose) {
        return ("bistra/user-secret/" + VERSION + "/" + purpose + "/" + secret.getOwner()
                + "/" + secret.getId() + "/" + secret.getName() + "/" + secret.getType().name())
                .getBytes(StandardCharsets.UTF_8);
    }

    private void requireEnvelope(UserSecretEnvelope envelope) {
        if (envelope == null || envelope.getVersion() != VERSION) {
            throw new UserSecretException(UNSUPPORTED_FORMAT);
        }
        if (envelope.getIv() == null || envelope.getIv().length != 12
                || envelope.getKeyIv() == null || envelope.getKeyIv().length != 12
                || envelope.getWrappedKey() == null || envelope.getWrappedKey().length != 48
                || envelope.getCiphertext() == null || envelope.getCiphertext().length < 16) {
            throw new UserSecretException(SECRET_INTEGRITY_FAILURE);
        }
    }
}
