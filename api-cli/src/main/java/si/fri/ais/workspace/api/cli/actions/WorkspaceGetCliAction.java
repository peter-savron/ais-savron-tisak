package si.fri.ais.workspace.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.workspace.api.cli.WorkspaceCliAction;

@Dependent
@CommandLine.Command(name = "get", description = "Show a workspace", mixinStandardHelpOptions = true)
public class WorkspaceGetCliAction implements WorkspaceCliAction {
    private final WorkspaceCliResolver resolver;
    private final WorkspaceCliPrinter printer;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace code name")
    private String name;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Acting logged-in user")
    private String user;

    @CommandLine.Option(names = {"-I", "--no-interactive"}, description = "Disable interactive prompts")
    private boolean noInteractive;

    public WorkspaceGetCliAction(WorkspaceCliResolver resolver,
                                 WorkspaceCliPrinter printer) {
        this.resolver = resolver;
        this.printer = printer;
    }

    @Override
    public void run() {
        var userId = resolver.actingUserId(user);
        printer.print(resolver.workspace(userId, name, resolver.interactive(noInteractive)));
    }
}
