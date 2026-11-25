package si.fri.ais.common.integration.local.arcadedb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbChangelog;

import java.util.List;

public interface ArcadeDbChangelogRepository {
    void runChangelogs(@NotNull List<@NotNull @Valid ArcadeDbChangelog> changelogs);
}
