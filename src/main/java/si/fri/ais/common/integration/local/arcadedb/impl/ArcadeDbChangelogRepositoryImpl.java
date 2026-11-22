package si.fri.ais.common.integration.local.arcadedb.impl;

import com.arcadedb.database.Database;
import com.arcadedb.database.Document;
import com.arcadedb.database.MutableDocument;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema;
import com.arcadedb.schema.Type;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.common.integration.local.arcadedb.ArcadeDbChangelogRepository;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbChangelog;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbChangelogExecution;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbVersion;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Startup
@ApplicationScoped
public class ArcadeDbChangelogRepositoryImpl implements ArcadeDbChangelogRepository {
    public static final String CHANGELOG_TYPE = "ArcadeDbChangelog";
    public static final String PROP_PARTITION = "partition";
    public static final String PROP_VERSION = "version";
    public static final String PROP_NAME = "name";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_RUN_ALWAYS = "runAlways";
    public static final String PROP_EXECUTED_AT = "executedAt";
    public static final String PROP_EXECUTION_TIME_MS = "executionTimeMs";
    public static final String PROP_SUCCESS = "success";

    private final Database database;
    private final ReentrantLock executionLock = new ReentrantLock();

    public ArcadeDbChangelogRepositoryImpl(Database database) {
        this.database = database;
    }

    @PostConstruct
    private void initialize() {
        // TODO clean at start option

        // Initialize DB schema
        Schema dbSchema = database.getSchema();

        DocumentType type = dbSchema.getOrCreateDocumentType(CHANGELOG_TYPE);

        if (!type.existsProperty(PROP_PARTITION)) {
            type.createProperty(PROP_PARTITION, Type.STRING);
        }
        if (!type.existsProperty(PROP_VERSION)) {
            type.createProperty(PROP_VERSION, Type.STRING);
        }
        if (!type.existsProperty(PROP_NAME)) {
            type.createProperty(PROP_NAME, Type.STRING);
        }
        if (!type.existsProperty(PROP_DESCRIPTION)) {
            type.createProperty(PROP_DESCRIPTION, Type.STRING);
        }
        if (!type.existsProperty(PROP_RUN_ALWAYS)) {
            type.createProperty(PROP_RUN_ALWAYS, Type.BOOLEAN);
        }

        if (!type.existsProperty(PROP_EXECUTED_AT)) {
            type.createProperty(PROP_EXECUTED_AT, Type.DATETIME);
        }
        if (!type.existsProperty(PROP_EXECUTION_TIME_MS)) {
            type.createProperty(PROP_EXECUTION_TIME_MS, Type.LONG);
        }
        if (!type.existsProperty(PROP_SUCCESS)) {
            type.createProperty(PROP_SUCCESS, Type.BOOLEAN);
        }

        // Composite unique index on (schema, version): prevents duplicate migrations per schema
        // while still serving prefix queries filtering by 'schema' alone.
        type.getOrCreateTypeIndex(Schema.INDEX_TYPE.LSM_TREE, true, PROP_PARTITION, PROP_VERSION);
    }

    @Override
    public void runChangelogs(@NotNull List<@NotNull @Valid ArcadeDbChangelog> changelogs) {
        if (changelogs.isEmpty()) {
            return;
        }

        executionLock.lock();
        try {
            validateChangelogs(changelogs);

            // Group by partition
            Map<String, List<ArcadeDbChangelog>> byPartition = changelogs.stream()
                    .collect(Collectors.groupingBy(ArcadeDbChangelog::partition));

            for (Map.Entry<String, List<ArcadeDbChangelog>> entry : byPartition.entrySet()) {
                String partition = entry.getKey();
                List<ArcadeDbChangelog> sortedChangelogs = entry.getValue().stream()
                        .sorted(Comparator.comparing(ArcadeDbChangelog::version))
                        .toList();

                Map<ArcadeDbVersion, ArcadeDbChangelogExecution> executedByVersion = loadExecutedRecords(partition);
                validateVersionOrdering(partition, sortedChangelogs, executedByVersion);

                for (ArcadeDbChangelog changelog : sortedChangelogs) {
                    ArcadeDbChangelogExecution existing = executedByVersion.get(changelog.version());

                    if (shouldExecute(changelog, existing)) {
                        executeAndRecord(changelog, existing != null);
                    }
                }
            }
        } finally {
            executionLock.unlock();
        }
    }

    private void validateChangelogs(List<ArcadeDbChangelog> changelogs) {
        Set<String> seenKeys = new HashSet<>();
        for (ArcadeDbChangelog changelog : changelogs) {
            String key = changelog.partition() + ":" + changelog.version();
            if (!seenKeys.add(key)) {
                throw new IllegalArgumentException(
                        "Duplicate changelog version detected for partition '%s': %s"
                                .formatted(changelog.partition(), changelog.version())
                );
            }
        }
    }

    private void validateVersionOrdering(
            String partition,
            List<ArcadeDbChangelog> sortedChangelogs,
            Map<ArcadeDbVersion, ArcadeDbChangelogExecution> executedByVersion
    ) {
        ArcadeDbVersion maxExecutedVersion = executedByVersion.values().stream()
                .filter(ArcadeDbChangelogExecution::success)
                .map(ArcadeDbChangelogExecution::version)
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (maxExecutedVersion == null) {
            return;
        }

        for (ArcadeDbChangelog changelog : sortedChangelogs) {
            ArcadeDbChangelogExecution existing = executedByVersion.get(changelog.version());

            if (existing == null && changelog.version().compareTo(maxExecutedVersion) < 0) {
                throw new IllegalStateException(
                        "Out-of-order changelog '%s' (version %s) for partition '%s': version is lower than last executed version %s"
                                .formatted(changelog.name(), changelog.version(), partition, maxExecutedVersion)
                );
            }
        }
    }

    private boolean shouldExecute(ArcadeDbChangelog changelog, ArcadeDbChangelogExecution existing) {
        if (existing == null) {
            return true; // Never executed
        }
        if (!Boolean.TRUE.equals(existing.success())) {
            return true; // Retry previously failed changelog
        }
        return changelog.runAlways(); // Re-run if marked runAlways
    }

    private void executeAndRecord(ArcadeDbChangelog changelog, boolean replaceExisting) {
        Instant startedAt = Instant.now();
        long startNanos = System.nanoTime();
        boolean success;
        Optional<RuntimeException> failure = Optional.empty();

        try {
            changelog.change().accept(database);
            success = true;
        } catch (Exception e) {
            success = false;
            failure = Optional.of(new IllegalStateException(
                    "Changelog '%s' (version %s) failed for partition '%s'"
                            .formatted(changelog.name(), changelog.version(), changelog.partition()),
                    e
            ));
        }

        Duration duration = Duration.ofNanos(System.nanoTime() - startNanos);

        ArcadeDbChangelogExecution execution = new ArcadeDbChangelogExecution(
                changelog.partition(),
                changelog.version(),
                changelog.name(),
                changelog.description(),
                changelog.change(),
                changelog.runAlways(),
                startedAt,
                duration,
                success
        );

        saveOrUpdateExecutionRecord(execution, replaceExisting);

        failure.ifPresent(e -> {throw  e;});
    }

    private void saveOrUpdateExecutionRecord(ArcadeDbChangelogExecution execution, boolean updateExisting) {
        MutableDocument doc = null;

        if (updateExisting) {
            String query = "SELECT FROM " + CHANGELOG_TYPE
                    + " WHERE " + PROP_PARTITION + " = :partition AND " + PROP_VERSION + " = :version";

            try (ResultSet rs = database.query("sql", query, Map.of(
                    "partition", execution.partition(),
                    "version", execution.version().toString()
            ))) {
                if (rs.hasNext()) {
                    doc = rs.next().getElement().orElseThrow().modify();
                }
            }
        }

        if (doc == null) {
            doc = database.newDocument(CHANGELOG_TYPE)
                    .set(PROP_PARTITION, execution.partition())
                    .set(PROP_VERSION, execution.version().toString());
        }

        doc.set(PROP_NAME, execution.name())
                .set(PROP_DESCRIPTION, execution.description())
                .set(PROP_RUN_ALWAYS, execution.runAlways())
                .set(PROP_EXECUTED_AT, Date.from(execution.executedAt()))
                .set(PROP_EXECUTION_TIME_MS, execution.executionTimeMs().toMillis())
                .set(PROP_SUCCESS, execution.success())
                .save();
    }


    private Map<ArcadeDbVersion, ArcadeDbChangelogExecution> loadExecutedRecords(String partition) {
        Map<ArcadeDbVersion, ArcadeDbChangelogExecution> records = new HashMap<>();
        String query = "SELECT FROM " + CHANGELOG_TYPE + " WHERE " + PROP_PARTITION + " = :partition";

        try (ResultSet rs = database.query("sql", query, Map.of("partition", partition))) {
            while (rs.hasNext()) {
                Result result = rs.next();
                Document doc = result.getElement().orElseThrow();
                ArcadeDbChangelogExecution execution = deserializeExecution(doc);
                records.put(execution.version(), execution);
            }
        }
        return records;
    }

    private ArcadeDbChangelogExecution deserializeExecution(Document doc) {
        Date executedAtDate = doc.getDate(PROP_EXECUTED_AT);
        Long durationMs = doc.getLong(PROP_EXECUTION_TIME_MS);

        return new ArcadeDbChangelogExecution(
                doc.getString(PROP_PARTITION),
                ArcadeDbVersion.of(doc.getString(PROP_VERSION)),
                doc.getString(PROP_NAME),
                doc.getString(PROP_DESCRIPTION),
                null,
                Boolean.TRUE.equals(doc.getBoolean(PROP_RUN_ALWAYS)),
                executedAtDate != null ? executedAtDate.toInstant() : null,
                durationMs != null ? Duration.ofMillis(durationMs) : Duration.ZERO,
                Boolean.TRUE.equals(doc.getBoolean(PROP_SUCCESS))
        );
    }
}