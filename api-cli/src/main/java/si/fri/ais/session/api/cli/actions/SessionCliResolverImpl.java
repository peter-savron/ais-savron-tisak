package si.fri.ais.session.api.cli.actions;

import jakarta.enterprise.context.ApplicationScoped;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import si.fri.ais.common.api.cli.prompts.LinePrompt;
import si.fri.ais.common.core.prompts.SelectionPrompt;
import si.fri.ais.session.api.cli.exceptions.SessionCliException;
import si.fri.ais.session.api.cli.exceptions.SessionCliExceptionMapper;
import si.fri.ais.session.core.models.Session;
import si.fri.ais.session.core.models.SessionSlot;
import si.fri.ais.session.core.ports.outgoing.SessionService;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.ports.outgoing.UserService;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static si.fri.ais.session.api.cli.exceptions.SessionCliException.Code.*;

@ApplicationScoped
public class SessionCliResolverImpl implements SessionCliResolver {
    private final LineReader reader;
    private final SelectionPrompt selectionPrompt;
    private final UserService users;
    private final SessionService sessions;

    public SessionCliResolverImpl(LineReader reader, SelectionPrompt selectionPrompt, UserService users, SessionService sessions) {
        this.reader = reader;
        this.selectionPrompt = selectionPrompt;
        this.users = users;
        this.sessions = sessions;
    }

    @Override
    public SessionSlot slot(boolean global, String name) {
        if (global) {
            return SessionSlot.builder().id(SessionSlot.GLOBAL_ID).build();
        }
        if (name != null) {
            return SessionSlot.builder().id(sessionName(name)).build();
        }
        ProcessHandle shell = ProcessHandle.current().parent()
                .orElseThrow(() -> new SessionCliException(SHELL_UNAVAILABLE));
        return SessionSlot.builder()
                .id(Long.toString(shell.pid()))
                .shellStartedAt(shell.info().startInstant().orElse(null))
                .build();
    }

    @Override
    public String sessionName(String name) {
        if (!name.matches(SessionSlot.NAME_FORMAT)) {
            throw new SessionCliException(INVALID_SESSION_NAME);
        }
        return name;
    }

    @Override
    public UUID userId(String name, boolean keepCurrent) {
        if (name != null) {
            return findUser(name);
        }
        requireInteractive(keepCurrent ? USER_OR_WORKSPACE_REQUIRED : USER_REQUIRED);
        while (true) {
            String input = LinePrompt.read(reader, keepCurrent ? "User name (Enter to keep): " : "User name: ");
            if (keepCurrent && input.isEmpty()) {
                return null;
            }
            try {
                return findUser(input);
            } catch (SessionCliException e) {
                reader.printAbove(SessionCliExceptionMapper.messageFor(e));
            }
        }
    }

    @Override
    public void requireInteractive(SessionCliException.Code code) {
        if (Terminal.TYPE_DUMB.equals(reader.getTerminal().getType())
                || Terminal.TYPE_DUMB_COLOR.equals(reader.getTerminal().getType())) {
            throw new SessionCliException(code);
        }
    }

    @Override
    public String chooseNamedSession() {
        List<Session> candidates = users.list().stream()
                .filter(User::isLogged)
                .flatMap(user -> sessions.listNamed(user.getId()).stream())
                .sorted(Comparator.comparing(Session::getLastAccessedAt).reversed().thenComparing(Session::getId))
                .toList();
        if (candidates.isEmpty()) {
            throw new SessionCliException(NO_NAMED_SESSIONS);
        }
        Session selected = selectionPrompt.select(candidates,
                session -> "%s  %s".formatted(session.getId(), session.getLastAccessedAt()));
        return selected.getId();
    }

    @Override
    public void reportSelectionChanged() {
        reader.printAbove("The selected session is no longer available. Choose again.");
    }

    @Override
    public void validateWorkspaceOption(String name) {
        if (name != null) {
            throw new SessionCliException(WORKSPACE_LOOKUP_UNAVAILABLE);
        }
    }

    @Override
    public void promptToKeepWorkspace() {
        while (!LinePrompt.read(reader, "Workspace name (Enter to keep): ").isEmpty()) {
            reader.printAbove("Workspace name lookup is unavailable. Press Enter to keep the current workspace.");
        }
    }

    private UUID findUser(String name) {
        if (!name.matches(SessionSlot.NAME_FORMAT)) {
            throw new SessionCliException(INVALID_USER_NAME);
        }
        return users.getUser(name)
                .orElseThrow(() -> new SessionCliException(USER_NOT_FOUND, name))
                .getId();
    }
}
