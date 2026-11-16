package si.fri.ais.common.core.integration.local.arcadedb.models;

import com.arcadedb.database.Database;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

/**
 * Entry of ArcadeDB changelog.
 * @param partition Splits changelogs into partitions, so there can be separate changelogs for each repository.
 * @param version Used to determine sequence in which to execute changelogs that were not yet run.
 * @param name Name of the changelog.
 * @param description Optional description.
 * @param change changes to be applied to ArcadeDb the database instance will be passed to the changelog.
 */
public record ArcadeDbChangelogExecution(
        @NotEmpty String partition,
        @NotNull @Valid ArcadeDbVersion version,
        @NotEmpty String name,
        String description,
        @JsonIgnore
        @NotNull Consumer<Database> change,
        @NotNull Boolean runAlways,
        @NotNull Instant executedAt,
        @NotNull Duration executionTimeMs,
        @NotNull Boolean success
) {
}
