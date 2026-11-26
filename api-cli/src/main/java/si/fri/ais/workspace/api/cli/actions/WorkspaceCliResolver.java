package si.fri.ais.workspace.api.cli.actions;

import jakarta.enterprise.context.ApplicationScoped;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import si.fri.ais.common.api.cli.prompts.LinePrompt;
import si.fri.ais.common.core.models.NamedObject;
import si.fri.ais.common.core.prompts.SelectionPrompt;
import si.fri.ais.session.api.cli.actions.SessionCliResolver;
import si.fri.ais.session.api.cli.exceptions.SessionCliException;
import si.fri.ais.session.core.models.ResolvedSession;
import si.fri.ais.session.core.models.SessionSlot;
import si.fri.ais.session.core.models.commands.SessionResolve;
import si.fri.ais.session.core.ports.outgoing.SessionService;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.ports.outgoing.UserService;
import si.fri.ais.workspace.api.cli.exceptions.WorkspaceCliException;
import si.fri.ais.workspace.core.exceptions.WorkspaceException;
import si.fri.ais.workspace.core.models.Workspace;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;

import java.util.UUID;
import java.util.regex.Pattern;

import static si.fri.ais.workspace.api.cli.exceptions.WorkspaceCliException.Code.DESCRIPTION_TOO_LONG;
import static si.fri.ais.workspace.api.cli.exceptions.WorkspaceCliException.Code.INVALID_NAME;
import static si.fri.ais.workspace.api.cli.exceptions.WorkspaceCliException.Code.LOGIN_REQUIRED;
import static si.fri.ais.workspace.api.cli.exceptions.WorkspaceCliException.Code.NAME_REQUIRED_FOR_SELECTION;
import static si.fri.ais.workspace.api.cli.exceptions.WorkspaceCliException.Code.NO_WORKSPACES;
import static si.fri.ais.workspace.api.cli.exceptions.WorkspaceCliException.Code.USER_NOT_FOUND;
import static si.fri.ais.workspace.api.cli.exceptions.WorkspaceCliExceptionMapper.NAME_RULES;

@ApplicationScoped
public class WorkspaceCliResolver {
    private static final Pattern NAME = Pattern.compile(NamedObject.NAME_FORMAT);

    private final LineReader reader;
    private final SelectionPrompt selection;
    private final SessionCliResolver sessionSlots;
    private final SessionService sessions;
    private final UserService users;
    private final WorkspaceService workspaces;

    public WorkspaceCliResolver(LineReader reader, SelectionPrompt selection,
                                SessionCliResolver sessionSlots, SessionService sessions,
                                UserService users, WorkspaceService workspaces) {
        this.reader = reader;
        this.selection = selection;
        this.sessionSlots = sessionSlots;
        this.sessions = sessions;
        this.users = users;
        this.workspaces = workspaces;
    }

    UUID actingUserId(String name) {
        if (name != null) {
            validateName(name, "user name");
            User user = users.getUser(name)
                    .orElseThrow(() -> new WorkspaceCliException(USER_NOT_FOUND, name));
            if (!user.isLogged()) {
                throw new WorkspaceCliException(LOGIN_REQUIRED, name);
            }
            return user.getId();
        }

        return sessions.resolveDefaults(SessionResolve.builder().slot(sessionSlots.slot(false, null)).build())
                .or(() -> sessions.resolveDefaults(SessionResolve.builder().slot(sessionSlots.slot(true, null)).build()))
                .map(ResolvedSession::getEffectiveSession)
                .orElseThrow(() -> new SessionCliException(
                        SessionCliException.Code.SESSION_INACTIVE, SessionSlot.GLOBAL_ID))
                .getUserId();
    }

    Workspace workspace(UUID userId, String name, boolean interactive) {
        if (name != null) {
            validateName(name, "workspace name");
            return workspaces.getWorkspace(userId, name)
                    .orElseThrow(() -> new WorkspaceException(WorkspaceException.Code.WORKSPACE_NOT_FOUND));
        }
        if (!interactive) {
            throw new WorkspaceCliException(NAME_REQUIRED_FOR_SELECTION);
        }
        var candidates = workspaces.list(userId);
        if (candidates.isEmpty()) {
            throw new WorkspaceCliException(NO_WORKSPACES);
        }
        return selection.select(candidates, Workspace::getName);
    }

    boolean interactive(boolean disabled) {
        String terminalType = reader.getTerminal().getType();
        return !disabled && !Terminal.TYPE_DUMB.equals(terminalType) && !Terminal.TYPE_DUMB_COLOR.equals(terminalType);
    }

    String promptName(String message, boolean keepCurrent) {
        while (true) {
            String input = LinePrompt.read(reader, message);
            if (keepCurrent && input.isEmpty()) {
                return null;
            }
            if (NAME.matcher(input).matches()) {
                return input;
            }
            reader.printAbove(NAME_RULES);
        }
    }

    void validateName(String name, String field) {
        if (!NAME.matcher(name).matches()) {
            throw new WorkspaceCliException(INVALID_NAME, field);
        }
    }

    void validateDescription(String description) {
        if (description != null && description.length() > Workspace.DESCRIPTION_LIMIT) {
            throw new WorkspaceCliException(DESCRIPTION_TOO_LONG);
        }
    }
}
