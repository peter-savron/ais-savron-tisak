package si.fri.ais.user.core.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.common.core.models.NamedObject;

import java.util.UUID;

/** Persistence representation of a secret. Never contains a plaintext value. */
@Getter
@Setter
@SuperBuilder
public class StoredUserSecret extends NamedObject {
    /** User UUID defining ownership and the name's uniqueness scope. */
    @NotNull private UUID owner;
    /** Describes the caller's encoding of the opaque plaintext bytes. */
    @NotNull private UserSecret.Type type;
    /** Authenticated ciphertext and wrapped key, both bound to this record. */
    @NotNull @Valid private UserSecretEnvelope envelope;
}
