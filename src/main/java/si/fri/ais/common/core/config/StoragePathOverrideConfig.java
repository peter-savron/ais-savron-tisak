package si.fri.ais.common.core.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Optional;

@ConfigMapping(prefix = "bistra.storage")
public interface StoragePathOverrideConfig {
    @WithName("cache.path")
    Optional<String> cachePath();

    @WithName("data.path")
    Optional<String> dataPath();

    @WithName("config.path")
    Optional<String> configPath();
}
