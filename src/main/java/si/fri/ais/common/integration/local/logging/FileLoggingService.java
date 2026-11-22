package si.fri.ais.common.integration.local.logging;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.Logger;
import org.jboss.logmanager.formatters.PatternFormatter;
import org.jboss.logmanager.handlers.PeriodicRotatingFileHandler;
import si.fri.ais.common.core.config.StorageConfig;
import si.fri.ais.common.integration.local.logging.config.CliLoggingConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;


@Startup
@Priority(1)
@ApplicationScoped
public class FileLoggingService {
    private final StorageConfig storageConfig;
    private final CliLoggingConfig cliLoggingConfig;

    private PeriodicRotatingFileHandler fileHandler;

    public FileLoggingService(StorageConfig storageConfig, CliLoggingConfig cliLoggingConfig) {
        this.storageConfig = storageConfig;
        this.cliLoggingConfig = cliLoggingConfig;
    }

    @PostConstruct
    public void init() {
        if (fileHandler != null) {
            return;
        }

        try {
            Path logDir = storageConfig.cachePath().resolve("logs");
            Files.createDirectories(logDir);
            File logFile = logDir.resolve(cliLoggingConfig.fileName()).toFile();

            fileHandler = new PeriodicRotatingFileHandler(
                    logFile,
                    cliLoggingConfig.rotationSuffix(),
                    cliLoggingConfig.append()
            );
            fileHandler.setFormatter(new PatternFormatter(cliLoggingConfig.pattern()));
            fileHandler.setLevel(Level.ALL);
            fileHandler.setAutoFlush(true);

            LogContext logContext = LogContext.getLogContext();

            Logger rootLogger = logContext.getLogger("");
            rootLogger.setLevel(cliLoggingConfig.level());
            rootLogger.addHandler(fileHandler);

            cliLoggingConfig.categories().forEach((packageName, categoryConfig) -> {
                Logger packageLogger = logContext.getLogger(packageName);
                packageLogger.setLevel(categoryConfig.level());
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize dynamic file logging", e);
        }
    }

    @PreDestroy
    void shutdown() {
        if (fileHandler != null) {
            LogContext.getLogContext().getLogger("").removeHandler(fileHandler);
            fileHandler.close();
        }
    }
}


