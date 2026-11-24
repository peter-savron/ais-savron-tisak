package si.fri.ais.session.api.cli.actions;

import jakarta.enterprise.context.ApplicationScoped;
import org.jline.terminal.Terminal;
import si.fri.ais.common.core.models.NamedObject;
import si.fri.ais.session.core.models.ResolvedSession;
import si.fri.ais.session.core.models.Session;
import si.fri.ais.session.core.models.SessionSlot;
import si.fri.ais.user.core.ports.outgoing.UserService;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;

import java.io.PrintWriter;

@ApplicationScoped
public class SessionCliPrinter {
    private final Terminal terminal;
    private final UserService users;
    private final WorkspaceService workspaces;

    public SessionCliPrinter(Terminal terminal, UserService users, WorkspaceService workspaces) {
        this.terminal = terminal;
        this.users = users;
        this.workspaces = workspaces;
    }

    void print(ResolvedSession resolved) {
        printSession(resolved.getSession(), "");
        if (resolved.getLinkedSession() != null) {
            terminal.writer().println("linkedSession:");
            printSession(resolved.getLinkedSession(), "  ");
        }
        terminal.flush();
    }

    String kind(SessionSlot slot) {
        if (slot.isTerminal()) {
            return "terminal";
        }
        return SessionSlot.GLOBAL_ID.equals(slot.getId()) ? "global" : "named";
    }

    private void printSession(Session session, String indent) {
        PrintWriter out = terminal.writer();
        out.printf("%skind: %s%n", indent, kind(session.getSlot()));
        out.printf("%sid: %s%n", indent, session.getId());
        out.printf("%suser: %s (%s)%n", indent,
                users.getUser(session.getUserId()).map(NamedObject::getName).orElse("unavailable"),
                session.getUserId());
        if (session.getWorkspaceId() == null) {
            out.printf("%sworkspace: unset%n", indent);
        } else {
            out.printf("%sworkspace: %s (%s)%n", indent,
                    workspaces.getWorkspace(session.getWorkspaceId()).map(NamedObject::getName).orElse("unavailable"),
                    session.getWorkspaceId());
        }
        out.printf("%slastAccessedAt: %s%n", indent, session.getLastAccessedAt());
        out.printf("%sexpiresAt: %s%n", indent, session.getExpiresAt());
        if (session.getUsesSession() != null) {
            out.printf("%susesSession: %s%n", indent, session.getUsesSession());
        }
    }
}
