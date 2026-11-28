package si.fri.ais.user.core.ports.incoming;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.UserKey;
import si.fri.ais.user.core.models.StoredUserSecret;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores encrypted envelopes only. Authorization belongs to core services.
 * Writes are atomic and report failures as {@code UserSecretException}, preserving
 * underlying causes. Enforce unique IDs and unique (owner, name) pairs at storage.
 */
public interface UserSecretRepository {
    /** Looks up an encrypted record; a missing ID returns empty. */
    Optional<StoredUserSecret> getSecret(@NotNull UUID secretId);

    /** Looks up an encrypted record using its owner-scoped name. */
    Optional<StoredUserSecret> getSecret(@NotNull UUID userId, @NotEmpty String secretName);

    /** Returns this owner's encrypted records ordered by name. */
    List<StoredUserSecret> list(@NotNull UUID userId);

    /**
     * Atomically rewraps all this user's data keys without decrypting the values.
     * If any key cannot be authenticated or persisted, no records may change.
     * The caller coordinates this with password/key replacement and excludes
     * concurrent secret mutations for the entire password-change operation.
     * @param userId user id
     * @param old old master encryption key
     * @param current new master encryption key
     */
    void updateKek(@NotNull UUID userId, @NotNull UserKey old, @NotNull UserKey current);

    /** Creates the encrypted record; owner/name conflicts use NAME_NOT_UNIQUE. */
    void createSecret(@NotNull @Valid StoredUserSecret secret);

    /** Updates only the envelope and type of the existing ID/owner/name in place. */
    void updateSecret(@NotNull @Valid StoredUserSecret secret);

    /** Deletes the existing record matching its ID and owner; never requires plaintext. */
    void deleteSecret(@NotNull @Valid StoredUserSecret secret);
}
