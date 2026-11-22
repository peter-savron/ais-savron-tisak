package si.fri.ais.common.integration.local.logging.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Map;
import java.util.logging.Level;

@ConfigMapping(prefix = "cli.logging")
public interface CliLoggingConfig {
    @WithName("file-name")
    @WithDefault("app.log")
    String fileName();

    @WithDefault("INFO")
    Level level();

    @WithName("rotation-suffix")
    @WithDefault(".yyyy-MM-dd")
    String rotationSuffix();

    @WithDefault("true")
    boolean append();

    @WithDefault("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c{3.}] (%t) %s%e%n")
    String pattern();

    /**
     * Maps package/category names to log levels, e.g.:
     * cli.logging.category."si.fri.ais".level=DEBUG
     */
    @WithName("category")
    Map<String, CategoryConfig> categories();

    interface CategoryConfig {
        @WithDefault("INFO")
        Level level();
    }
}
