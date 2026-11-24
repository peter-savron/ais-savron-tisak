package si.fri.ais.common.integration.local.arcadedb.providers;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import lombok.extern.jbosslog.JBossLog;
import si.fri.ais.common.core.config.StorageConfig;

import java.nio.file.Files;
import java.nio.file.Path;

@JBossLog
@ApplicationScoped
public class ArcadeDbProvider {

    private final StorageConfig storageConfig;

    private DatabaseFactory factory;
    private Database database;

    public ArcadeDbProvider(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @PostConstruct
    void init() {
        try {
            Path arcadeDbDir = storageConfig.dataPath().resolve(".arcadedb");
            Files.createDirectories(arcadeDbDir);
            log.debug("Initializing embedded ArcadeDB at path: %s".formatted(arcadeDbDir.toAbsolutePath()));
            factory = new DatabaseFactory(arcadeDbDir.toAbsolutePath().toString());

            if (factory.exists()) {
                database = factory.open();
                log.info("Opened existing ArcadeDB instance.");
            } else {
                database = factory.create();
                log.info("Created new ArcadeDB instance.");
            }
        } catch (Exception e) {
            log.error("Could not initialize ArcadeDb.", e);
            throw new RuntimeException(e);
        }
    }

    @Produces
    @Singleton
    public Database produceDatabase() {
        return database;
    }

    @PreDestroy
    void close() {
        log.info("Shutting down embedded ArcadeDB...");
        try {
            if (database != null && database.isOpen()) {
                database.close();
            }
        } catch (Exception e) {
            log.error("Could not close ArcadeDb database.", e);
        }
        try {
            if (factory != null) {
                factory.close();
            }
        } catch (Exception e) {
            log.error("Could not close ArcadeDb factory.", e);
        }
        log.info("ArcadeDB closed cleanly.");
    }
}