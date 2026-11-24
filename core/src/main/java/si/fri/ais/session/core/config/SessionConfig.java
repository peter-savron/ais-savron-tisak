package si.fri.ais.session.core.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.time.Duration;

@ConfigMapping(prefix = "bistra.session")
public interface SessionConfig {
    @WithName("local.idle-timeout")
    @WithDefault("PT24H")
    Duration localIdleTimeout();

    @WithName("named.idle-timeout")
    @WithDefault("P30D")
    Duration namedIdleTimeout();
}
