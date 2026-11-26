package si.fri.ais.workspace.api.cli.actions;

import jakarta.enterprise.context.ApplicationScoped;
import org.jline.terminal.Terminal;
import si.fri.ais.workspace.core.models.Workspace;

import java.util.List;

@ApplicationScoped
public class WorkspaceCliPrinter {
    private final Terminal terminal;

    public WorkspaceCliPrinter(Terminal terminal) {
        this.terminal = terminal;
    }

    void print(Workspace workspace) {
        var out = terminal.writer();
        out.printf("name: %s%n", workspace.getName());
        if (workspace.getTitle() != null) {
            out.printf("title: %s%n", workspace.getTitle());
        }
        if (workspace.getDescription() != null) {
            out.printf("description:%n%s%n", workspace.getDescription());
        }
        terminal.flush();
    }

    void printList(List<Workspace> workspaces) {
        if (workspaces.isEmpty()) {
            terminal.writer().println("You have no workspaces.");
        }
        workspaces.forEach(workspace -> terminal.writer().println(workspace.getName()));
        terminal.flush();
    }
}
