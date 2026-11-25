package si.fri.ais.workspace.integrations.local.impl;

import com.arcadedb.database.Database;
import com.arcadedb.database.Document;
import com.arcadedb.exception.DuplicatedKeyException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.common.core.config.StorageConfig;
import si.fri.ais.common.integration.local.arcadedb.ArcadeDbChangelogRepository;
import si.fri.ais.common.integration.local.arcadedb.scripts.CommonArcadeDbScripts;
import si.fri.ais.workspace.core.exceptions.WorkspaceException;
import si.fri.ais.workspace.core.models.Workspace;
import si.fri.ais.workspace.core.ports.incoming.WorkspaceRepository;
import si.fri.ais.workspace.integrations.local.mappers.WorkspaceDocumentMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static si.fri.ais.workspace.core.exceptions.WorkspaceException.Code.*;
import static si.fri.ais.workspace.integrations.local.values.WorkspaceArcadeDbValues.*;

@ApplicationScoped
public class WorkspaceRepositoryLocalImpl implements WorkspaceRepository {
    private final StorageConfig storageConfig;
    private final ArcadeDbChangelogRepository changelogRepository;
    private final WorkspaceDocumentMapper mapper;
    private final Database database;
    private final ReentrantLock operationLock = new ReentrantLock();

    public WorkspaceRepositoryLocalImpl(StorageConfig storageConfig, ArcadeDbChangelogRepository changelogRepository,
                                        WorkspaceDocumentMapper mapper, Database database) {
        this.storageConfig = storageConfig;
        this.changelogRepository = changelogRepository;
        this.mapper = mapper;
        this.database = database;
    }

    @PostConstruct
    void init() {
        changelogRepository.runChangelogs(CHANGELOGS);
    }

    @Override
    public Optional<Workspace> find(UUID workspaceId) {
        return CommonArcadeDbScripts.searchById(database, TYPE, workspaceId).map(mapper::fromDocument);
    }

    @Override
    public Optional<Workspace> findByName(UUID ownerId, String name) {
        String query = "SELECT FROM %s WHERE %s = ? AND %s = ?".formatted(TYPE, OWNER_ID, NAME);
        try (var results = database.query("sql", query, ownerId.toString(), name)) {
            return results.hasNext() ? results.next().getElement().map(mapper::fromDocument) : Optional.empty();
        }
    }

    @Override
    public List<Workspace> listByOwner(UUID ownerId) {
        String query = "SELECT FROM %s WHERE %s = ? ORDER BY %s".formatted(TYPE, OWNER_ID, NAME);
        try (var results = database.query("sql", query, ownerId.toString())) {
            return results.stream().map(result -> mapper.fromDocument(result.getElement().orElseThrow())).toList();
        }
    }

    @Override
    public void create(Workspace workspace) {
        operationLock.lock();
        try {
            Path directory = workspaceDirectory(workspace);
            boolean parentCreated = false;
            boolean directoryCreated = false;
            try {
                try {
                    Files.createDirectory(directory.getParent());
                    parentCreated = true;
                } catch (FileAlreadyExistsException existing) {
                    if (!Files.isDirectory(directory.getParent(), NOFOLLOW_LINKS)) {
                        throw existing;
                    }
                }
                Files.createDirectory(directory);
                directoryCreated = true;
                database.transaction(() -> {
                    var document = database.newDocument(TYPE);
                    mapper.initialize(workspace, document);
                    document.save();
                });
            } catch (IOException | RuntimeException failure) {
                if (directoryCreated) {
                    cleanUpDirectory(directory, failure);
                }
                if (parentCreated) {
                    cleanUpDirectory(directory.getParent(), failure);
                }
                if (failure instanceof IOException ioFailure) {
                    throw new UncheckedIOException("Could not create workspace directory", ioFailure);
                }
                throw translateWriteFailure((RuntimeException) failure);
            }
        } finally {
            operationLock.unlock();
        }
    }

    @Override
    public void update(Workspace workspace) {
        try {
            database.transaction(() -> {
                var document = requireDocument(workspace).modify();
                mapper.update(workspace, document);
                document.save();
            });
        } catch (DuplicatedKeyException failure) {
            throw translateWriteFailure(failure);
        }
    }

    @Override
    public void delete(Workspace workspace) {
        operationLock.lock();
        try {
            Path directory = workspaceDirectory(workspace);
            // Retain the filesystem outcome until the database transaction commits.
            boolean[] directoryRemoved = {false};
            try {
                database.transaction(() -> {
                    database.deleteRecord(requireDocument(workspace));
                    directoryRemoved[0] = removeWorkspaceDirectory(directory);
                });
            } catch (RuntimeException failure) {
                if (directoryRemoved[0]) {
                    try {
                        Files.createDirectory(directory);
                    } catch (IOException restoreFailure) {
                        failure.addSuppressed(restoreFailure);
                    }
                }
                throw failure;
            }
        } finally {
            operationLock.unlock();
        }
    }

    private Document requireDocument(Workspace workspace) {
        return CommonArcadeDbScripts.searchById(database, TYPE, workspace.getId())
                .filter(document -> workspace.getOwnerId().toString().equals(document.getString(OWNER_ID)))
                .orElseThrow(() -> new WorkspaceException(WORKSPACE_NOT_FOUND));
    }

    private Path workspaceDirectory(Workspace workspace) {
        Path owner = storageConfig.dataPath().resolve(workspace.getOwnerId().toString());
        Path parent = owner.resolve("workspaces");
        Path directory = parent.resolve(workspace.getId().toString());
        if (Files.isSymbolicLink(owner) || Files.isSymbolicLink(parent) || Files.isSymbolicLink(directory)) {
            throw new IllegalStateException("Workspace storage paths cannot be symbolic links");
        }
        return directory;
    }

    private boolean removeWorkspaceDirectory(Path directory) {
        try {
            if (Files.exists(directory, NOFOLLOW_LINKS) && !Files.isDirectory(directory, NOFOLLOW_LINKS)) {
                throw new WorkspaceException(DELETION_BLOCKED);
            }
            return Files.deleteIfExists(directory);
        } catch (DirectoryNotEmptyException failure) {
            throw new WorkspaceException(DELETION_BLOCKED);
        } catch (IOException failure) {
            throw new UncheckedIOException("Could not remove workspace directory", failure);
        }
    }

    private void cleanUpDirectory(Path directory, Throwable failure) {
        try {
            Files.deleteIfExists(directory);
        } catch (IOException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    private RuntimeException translateWriteFailure(RuntimeException failure) {
        if (failure instanceof DuplicatedKeyException duplicate) {
            var index = database.getSchema().getIndexByName(duplicate.getIndexName());
            if (index != null && index.getPropertyNames().equals(List.of(OWNER_ID, NAME))) {
                var conflict = new WorkspaceException(NAME_NOT_UNIQUE);
                conflict.initCause(failure);
                return conflict;
            }
        }
        return failure;
    }
}
