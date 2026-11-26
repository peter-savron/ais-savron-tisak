package si.fri.ais.spec.core.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;

/**
 * One entry in a provider's complete discovery result, with upstream facts and discovery status.
 *
 * @param externalId source-scoped lookup key accepted by the provider when opening the file
 * @param externalHash upstream content hash, when available
 * @param fileName display filename, when the provider supplies one
 * @param updatedAt upstream modification time, when available
 * @param movedFrom previous source-scoped key, when this item moved
 * @param movedTo current source-scoped key, when this item moved
 * @param status whether this item is unchanged, moved, modified or deleted upstream
 */
@Builder
public record RemoteSpec(@NotBlank String externalId, String externalHash,
                         String fileName, Instant updatedAt, String movedFrom, String movedTo,
                         @NotNull Status status) {
    public enum Status { UNCHANGED, MOVED, MODIFIED, DELETED }
}
