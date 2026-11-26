package si.fri.ais.spec.core.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/** Lightweight source-scoped projection used to reconcile a complete discovery result.
 *
 * @param id internal Spec UUID
 * @param externalId provider lookup key
 * @param hash last recorded retrieved-content hash, when available
 * @param externalHash last recorded upstream hash
 */
@Builder
public record SpecIdentifiers(@NotNull UUID id, @NotBlank String externalId, String hash, String externalHash) {}
