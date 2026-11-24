package si.fri.ais.session.core.ports.outgoing.impl;

import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.session.core.config.SessionConfig;
import si.fri.ais.session.core.exceptions.SessionException;
import si.fri.ais.session.core.models.ResolvedSession;
import si.fri.ais.session.core.models.Session;
import si.fri.ais.session.core.models.SessionSlot;
import si.fri.ais.session.core.models.commands.SessionDelete;
import si.fri.ais.session.core.models.commands.SessionGet;
import si.fri.ais.session.core.models.commands.SessionResolve;
import si.fri.ais.session.core.models.commands.SessionSet;
import si.fri.ais.session.core.models.commands.SessionStart;
import si.fri.ais.session.core.models.commands.SessionStop;
import si.fri.ais.session.core.models.commands.SessionUse;
import si.fri.ais.session.core.ports.incoming.SessionRepository;
import si.fri.ais.session.core.ports.outgoing.SessionService;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.ports.outgoing.UserService;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static si.fri.ais.session.core.exceptions.SessionException.Code.*;

@ApplicationScoped
public class SessionServiceImpl implements SessionService {
    private final SessionRepository repository;
    private final WorkspaceService workspace;
    private final UserService users;
    private final Duration localIdleTimeout;
    private final Duration namedIdleTimeout;

    public SessionServiceImpl(
            SessionRepository repository,
            WorkspaceService workspace,
            UserService users,
            SessionConfig config
    ) {
        Duration localIdleTimeout = config.localIdleTimeout();
        Duration namedIdleTimeout = config.namedIdleTimeout();
        if (!localIdleTimeout.isPositive() || !namedIdleTimeout.isPositive()) {
            throw new IllegalArgumentException("Session idle timeouts must be positive");
        }
        this.repository = repository;
        this.workspace = workspace;
        this.users = users;
        this.localIdleTimeout = localIdleTimeout;
        this.namedIdleTimeout = namedIdleTimeout;
    }

    @Override
    public Session start(SessionStart command) {
        requireUser(command.getUserId(), false);
        UUID workspaceId = command.getWorkspaceId();
        Optional.ofNullable(workspaceId).ifPresent(workspace::getWorkspace);
        SessionSlot slot = command.getSlot();
        Session session = Session.builder()
                .slot(slot)
                .userId(command.getUserId())
                .workspaceId(workspaceId)
                .lastAccessedAt(Instant.now())
                .build();
        session.setExpiresAt(session.getLastAccessedAt().plus(slot == null || slot.isTerminal() ? localIdleTimeout : namedIdleTimeout));
        if (slot != null) {
            repository.create(session);
        }
        return session;
    }

    @Override
    public Optional<ResolvedSession> get(SessionGet command) {
        return resolve(command.getSlot());
    }

    @Override
    public Session set(SessionSet command) {
        Session session = requireActive(command.getSlot());
        UUID userId = Optional.ofNullable(command.getUserId()).orElse(session.getUserId());
        UUID workspaceId = Optional.ofNullable(command.getWorkspaceId()).orElse(session.getWorkspaceId());
        requireUser(userId, false);
        Optional.ofNullable(workspaceId).ifPresent(workspace::getWorkspace);

        session.setUserId(userId);
        session.setWorkspaceId(workspaceId);
        session.setLastAccessedAt(Instant.now());
        session.setExpiresAt(session.getLastAccessedAt().plus(session.getSlot() == null || session.getSlot().isTerminal() ? localIdleTimeout : namedIdleTimeout));
        repository.update(session);
        return session;
    }

    @Override
    public ResolvedSession use(SessionUse command) {
        Session terminal = requireActive(command.getTerminal());
        Optional<Session> target = Optional.ofNullable(command.getTargetId()).map(this::requireTargetSession);
        Session effective = target.orElse(terminal);
        requireUser(effective.getUserId(), true);
        terminal.setUsesSession(target.map(Session::getId).orElse(null));
        return refresh(terminal, target.orElse(null));
    }

    @Override
    public Optional<ResolvedSession> resolveDefaults(SessionResolve command) {
        return resolve(command.getSlot());
    }

    @Override
    public void stop(SessionStop command) {
        repository.remove(command.getSlot());
    }

    @Override
    public void delete(SessionDelete command) {
        repository.deleteNamed(command.getName());
    }

    @Override
    public List<Session> listNamed(UUID userId) {
        requireUser(userId, true);
        return repository.listNamed(userId);
    }

    private Optional<ResolvedSession> resolve(SessionSlot slot) {
        return repository.find(slot).map(session -> {
            Optional<Session> target = Optional.ofNullable(session.getUsesSession()).map(this::requireTargetSession);
            requireUser(session.getUserId(), true);
            Session effective = target.orElse(session);
            if (!effective.getUserId().equals(session.getUserId())) {
                requireUser(effective.getUserId(), true);
            }
            return refresh(session, target.orElse(null));
        });
    }

    private Session requireActive(SessionSlot slot) {
        return repository.find(slot)
                .orElseThrow(() -> new SessionException(SESSION_INACTIVE, slot.getId()));
    }

    private Session requireTargetSession(String targetId) {
        return repository.find(SessionSlot.builder().id(targetId).build())
                .orElseThrow(() -> new SessionException(LINKED_SESSION_INACTIVE, targetId));
    }

    private void requireUser(UUID id, boolean loginRequired) {
        User user = users.getUser(id).orElseThrow(() -> new SessionException(USER_MISSING, id.toString()));
        if (loginRequired && !user.isLogged()) {
            throw new SessionException(LOGIN_REQUIRED, user.getName());
        }
    }

    private ResolvedSession refresh(Session session, Session target) {
        session.setLastAccessedAt(Instant.now());
        session.setExpiresAt(session.getLastAccessedAt().plus(session.getSlot() == null || session.getSlot().isTerminal() ? localIdleTimeout : namedIdleTimeout));
        if (target == null) {
            repository.update(session);
            return ResolvedSession.builder().session(session).build();
        }

        target.setLastAccessedAt(session.getLastAccessedAt());
        target.setExpiresAt(target.getLastAccessedAt().plus(target.getSlot() == null || target.getSlot().isTerminal() ? localIdleTimeout : namedIdleTimeout));
        repository.replace(List.of(session, target));
        return ResolvedSession.builder().session(session).linkedSession(target).build();
    }
}
