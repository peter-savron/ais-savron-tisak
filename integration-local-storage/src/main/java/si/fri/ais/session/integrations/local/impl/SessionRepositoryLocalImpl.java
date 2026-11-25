package si.fri.ais.session.integrations.local.impl;

import com.arcadedb.database.Database;
import com.arcadedb.database.Document;
import com.arcadedb.database.MutableDocument;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.common.integration.local.arcadedb.ArcadeDbChangelogRepository;
import si.fri.ais.session.core.exceptions.SessionException;
import si.fri.ais.session.core.models.Session;
import si.fri.ais.session.core.models.SessionSlot;
import si.fri.ais.session.core.ports.incoming.SessionRepository;
import si.fri.ais.session.integrations.local.mappers.SessionDocumentMapper;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static si.fri.ais.session.core.exceptions.SessionException.Code.*;
import static si.fri.ais.session.integrations.local.values.SessionArcadeDbValues.*;

@ApplicationScoped
public class SessionRepositoryLocalImpl implements SessionRepository {
    private final ArcadeDbChangelogRepository changelogRepository;
    private final Database database;
    private final SessionDocumentMapper mapper;
    private final ReentrantLock operationLock = new ReentrantLock();

    public SessionRepositoryLocalImpl(ArcadeDbChangelogRepository changelogRepository, Database database,
                                      SessionDocumentMapper mapper) {
        this.changelogRepository = changelogRepository;
        this.database = database;
        this.mapper = mapper;
    }

    @PostConstruct
    void init() {
        changelogRepository.runChangelogs(CHANGELOGS);
    }

    @Override
    public Optional<Session> find(SessionSlot slot) {
        return afterCleanup(() -> matching(slot).map(mapper::fromDocument));
    }

    @Override
    public List<Session> listNamed(UUID userId) {
        return afterCleanup(() -> selectBy(USER_ID, userId.toString()).stream()
                .filter(document -> document.getString(ID).matches(SessionSlot.NAME_FORMAT))
                .map(mapper::fromDocument)
                .sorted(Comparator.comparing(Session::getLastAccessedAt).reversed().thenComparing(Session::getId))
                .toList());
    }

    @Override
    public void create(Session session) {
        write(() -> {
            SessionSlot slot = Objects.requireNonNull(session.getSlot(), "A stored session requires a slot");
            Optional<Document> existing = byId(slot.getId());
            if (existing.filter(document -> !shellConflicts(slot, document)).isPresent()) {
                throw new SessionException(SLOT_OCCUPIED, slot.getId());
            }

            requireLinkTarget(session);
            existing.ifPresent(database::deleteRecord);
            MutableDocument document = database.newDocument(TYPE);
            mapper.initialize(session, document);
            document.save();
        });
    }

    @Override
    public void update(Session updated) {
        replace(List.of(updated));
    }

    @Override
    public void replace(List<Session> updated) {
        write(() -> {
            Set<String> ids = new HashSet<>();
            for (Session session : updated) {
                if (!ids.add(session.getId())) {
                    throw new IllegalArgumentException("Duplicate session slot: " + session.getId());
                }
                Document document = matching(Objects.requireNonNull(session.getSlot(), "A stored session requires a slot"))
                        .orElseThrow(() -> new SessionException(CONCURRENT_CHANGE, session.getId()));
                requireLinkTarget(session);
                MutableDocument mutable = document.modify();
                mapper.update(session, mutable);
                mutable.save();
            }
        });
    }

    @Override
    public void remove(SessionSlot slot) {
        write(() -> deleteAndClearLinks(List.of(matching(slot)
                .orElseThrow(() -> new SessionException(SESSION_INACTIVE, slot.getId())))));
    }

    @Override
    public void deleteNamed(String name) {
        write(() -> {
            if (!name.matches(SessionSlot.NAME_FORMAT)) {
                throw new IllegalArgumentException("Invalid named session identifier");
            }
            deleteAndClearLinks(List.of(byId(name)
                    .orElseThrow(() -> new SessionException(NAMED_SESSION_MISSING, name))));
        });
    }

    @Override
    public void removeByWorkspace(UUID workspaceId) {
        write(() -> deleteAndClearLinks(selectBy(WORKSPACE_ID, workspaceId.toString())));
    }

    @Override
    public void removeTerminalByUser(UUID userId) {
        write(() -> deleteAndClearLinks(selectBy(USER_ID, userId.toString()).stream()
                .filter(document -> SessionSlot.builder().id(document.getString(ID)).build().isTerminal())
                .toList()));
    }

    // Keep the committed cleanup and the requested operation under the same repository lock.
    private <T> T afterCleanup(Supplier<T> operation) {
        operationLock.lock();
        try {
            Instant now = Instant.now();
            database.transaction(() -> {
                List<Document> expired;
                try (var results = database.query("sql", "SELECT FROM " + TYPE)) {
                    expired = results.stream()
                            .map(result -> result.getElement().orElseThrow())
                            .filter(document -> !mapper.expiresAt(document).isAfter(now))
                            .toList();
                }
                deleteAndClearLinks(expired);
            });
            return operation.get();
        } finally {
            operationLock.unlock();
        }
    }

    private void write(Runnable operation) {
        afterCleanup(() -> {
            database.transaction(operation::run);
            return null;
        });
    }

    private Optional<Document> byId(String id) {
        try (var results = database.query("sql", "SELECT FROM " + TYPE + " WHERE " + ID + " = ?", id)) {
            return results.hasNext() ? results.next().getElement() : Optional.empty();
        }
    }

    private List<Document> selectBy(String field, String value) {
        try (var results = database.query("sql", "SELECT FROM " + TYPE + " WHERE " + field + " = ?", value)) {
            return results.stream().map(result -> result.getElement().orElseThrow()).toList();
        }
    }

    private Optional<Document> matching(SessionSlot slot) {
        return byId(slot.getId()).filter(document -> !shellConflicts(slot, document));
    }

    private boolean shellConflicts(SessionSlot slot, Document document) {
        Instant storedStart = mapper.shellStartedAt(document);
        return slot.isTerminal() && storedStart != null && slot.getShellStartedAt() != null
                && !storedStart.equals(slot.getShellStartedAt());
    }

    private void requireLinkTarget(Session session) {
        String target = session.getUsesSession();
        if (target != null && byId(target).isEmpty()) {
            throw new SessionException(CONCURRENT_CHANGE, target);
        }
    }

    private void deleteAndClearLinks(List<Document> documents) {
        Set<String> removedIds = documents.stream()
                .map(document -> document.getString(ID))
                .collect(Collectors.toSet());
        for (String id : removedIds) {
            if (id.matches(SessionSlot.SHARED_ID_FORMAT)) {
                selectBy(USES_SESSION, id).stream()
                        .filter(document -> !removedIds.contains(document.getString(ID)))
                        .forEach(document -> document.modify().set(USES_SESSION, null).save());
            }
        }
        documents.forEach(database::deleteRecord);
    }
}
