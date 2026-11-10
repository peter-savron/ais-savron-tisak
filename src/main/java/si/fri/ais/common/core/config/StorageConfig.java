package si.fri.ais.common.core.config;

import jakarta.enterprise.context.ApplicationScoped;

import java.nio.file.Path;
import java.nio.file.Paths;

import static si.fri.ais.common.core.utils.MainApplicationValues.APP_NAME;

@ApplicationScoped
public class StorageConfig {
    private static final String ENV_CACHE_PATH = "BISTRA_CACHE_PATH";
    private static final String ENV_DATA_PATH = "BISTRA_DATA_PATH";
    private static final String ENV_CONFIG_PATH = "BISTRA_CONFIG_PATH";

    private static final String WIN = "win";
    private static final String MAC = "mac";
    private static final String LOCALAPPDATA = "LOCALAPPDATA";
    private static final String APPDATA = "APPDATA";

    private final String os;
    private final String userHome;

    public StorageConfig() {
        this.os = System.getProperty("os.name").toLowerCase();
        this.userHome = System.getProperty("user.home");
    }

    public Path cachePath() {
        String envValue = System.getenv(ENV_CACHE_PATH);
        if (hasValue(envValue)) {
            return Paths.get(envValue);
        }
        return getSystemDefaultCachePath();
    }

    public Path dataPath() {
        String envValue = System.getenv(ENV_DATA_PATH);
        if (hasValue(envValue)) {
            return Paths.get(envValue);
        }
        return getSystemDefaultDataPath();
    }

    public Path configPath() {
        String envValue = System.getenv(ENV_CONFIG_PATH);
        if (hasValue(envValue)) {
            return Paths.get(envValue);
        }
        return getSystemDefaultConfigPath();
    }

    private boolean hasValue(String val) {
        return val != null && !val.isBlank();
    }

    private Path getSystemDefaultCachePath() {
        if (os.contains(WIN)) {
            String localAppData = System.getenv(LOCALAPPDATA);
            return Paths.get(localAppData != null ? localAppData : userHome, APP_NAME, "cache");
        } else if (os.contains(MAC)) {
            return Paths.get(userHome, "Library", "Caches", APP_NAME);
        } else {
            // Linux / Unix (XDG specification)
            String xdgCache = System.getenv("XDG_CACHE_HOME");
            return xdgCache != null ? Paths.get(xdgCache, APP_NAME) : Paths.get(userHome, ".cache", APP_NAME);
        }
    }

    private Path getSystemDefaultDataPath() {
        if (os.contains(WIN)) {
            String appData = System.getenv(APPDATA);
            return Paths.get(appData != null ? appData : userHome, APP_NAME, "data");
        } else if (os.contains(MAC)) {
            return Paths.get(userHome, "Library", "Application Support", APP_NAME);
        } else {
            // Linux / Unix (XDG specification)
            String xdgData = System.getenv("XDG_DATA_HOME");
            return xdgData != null ? Paths.get(xdgData, APP_NAME) : Paths.get(userHome, ".local", "share", APP_NAME);
        }
    }

    private Path getSystemDefaultConfigPath() {
        if (os.contains(WIN)) {
            String appData = System.getenv(APPDATA);
            return Paths.get(appData != null ? appData : userHome, APP_NAME, "config");
        } else if (os.contains(MAC)) {
            return Paths.get(userHome, "Library", "Preferences", APP_NAME);
        } else {
            // Linux / Unix (XDG specification)
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            return xdgConfig != null ? Paths.get(xdgConfig, APP_NAME) : Paths.get(userHome, ".config", APP_NAME);
        }
    }
}