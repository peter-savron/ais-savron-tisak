package si.fri.ais.spec.core.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;

/**
 * A published version of a Spec.
 *
 * @param id opaque revision identifier accepted by the diff operation for this Spec
 * @param publishedAt time when this version was published
 */
@Builder
public record RawSpecRevision(@NotBlank String id, @NotNull Instant publishedAt) {}
