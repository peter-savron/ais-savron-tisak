package si.fri.ais.user.core.ports.outgoing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.user.core.models.UserSecret;
import si.fri.ais.user.core.models.UserSecretContext;
import si.fri.ais.user.core.models.UserSecretMetadata;
import si.fri.ais.user.core.models.commands.UserSecretCreate;
import si.fri.ais.user.core.models.commands.UserSecretGet;
import si.fri.ais.user.core.models.commands.UserSecretUpdate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages user-owned secrets for trusted callers. Every operation requires a
 * logged-in owner. Failures use {@code UserSecretException} with stable codes.
 * Plaintext reveal additionally requires fresh password verification and a
 * stored user key. Never expose revealed values or credentials to agents.
 */
public interface UserSecretService {
    /** Returns only this owner's metadata, ordered by name, without decrypting. */
    List<UserSecretMetadata> list(@NotNull UUID userId);

    /**
     * Verifies the password before decrypting. Missing or foreign secrets return
     * empty. The caller must close the result to clear its plaintext bytes.
     */
    Optional<UserSecret> getSecret(@NotNull @Valid UserSecretGet get);

    /** Encrypts and creates a secret; duplicate owner/name pairs fail with NAME_NOT_UNIQUE. */
    UserSecretMetadata createSecret(@NotNull @Valid UserSecretCreate create);

    /** Replaces the value and type using a fresh data key, preserving ID, owner and name. */
    UserSecretMetadata updateSecret(@NotNull @Valid UserSecretUpdate update);

    /** Deletes the owner's secret without decrypting; absent/foreign secrets fail with SECRET_NOT_FOUND. */
    void deleteSecret(@NotNull @Valid UserSecretContext context);
}
