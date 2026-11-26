package si.fri.ais.workspace.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.workspace.api.cli.WorkspaceCliAction;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;

@Dependent
@CommandLine.Command(name = "list", description = "List the acting user's workspaces", mixinStandardHelpOptions = true)
public class WorkspaceListCliAction implements WorkspaceCliAction {
    private final WorkspaceCliResolver resolver;
    private final WorkspaceService workspaces;
    private final WorkspaceCliPrinter printer;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Acting logged-in user")
    private String user;

    @CommandLine.Option(names = {"-I", "--no-interactive"}, description = "Disable interactive prompts")
    private boolean noInteractive;

    public WorkspaceListCliAction(WorkspaceCliResolver resolver,
                                  WorkspaceService workspaces,
                                  WorkspaceCliPrinter printer) {
        this.resolver = resolver;
        this.workspaces = workspaces;
        this.printer = printer;
    }

    @Override
    public void run() {
        printer.printList(workspaces.list(resolver.actingUserId(user)));
    }
}
