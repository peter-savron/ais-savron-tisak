package si.fri.ais.workspace.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import org.jline.reader.LineReader;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.prompts.LinePrompt;
import si.fri.ais.common.api.cli.prompts.LongTextPrompt;
import si.fri.ais.workspace.api.cli.WorkspaceCliAction;
import si.fri.ais.workspace.core.models.Workspace;
import si.fri.ais.workspace.core.models.commands.WorkspaceCreate;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;

@Dependent
@CommandLine.Command(name = "create", description = "Create a workspace", mixinStandardHelpOptions = true)
public class WorkspaceCreateCliAction implements WorkspaceCliAction {
    private final WorkspaceCliResolver resolver;
    private final WorkspaceService workspaces;
    private final WorkspaceCliPrinter printer;
    private final LineReader reader;
    private final LongTextPrompt longText;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace code name")
    private String name;

    @CommandLine.Option(names = "--title", description = "Workspace title")
    private String title;

    @CommandLine.Option(names = "--description", description = "Workspace description")
    private String description;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Acting logged-in user")
    private String user;

    @CommandLine.Option(names = {"-I", "--no-interactive"}, description = "Disable interactive prompts")
    private boolean noInteractive;

    public WorkspaceCreateCliAction(WorkspaceCliResolver resolver,
                                    WorkspaceService workspaces,
                                    WorkspaceCliPrinter printer,
                                    LineReader reader,
                                    LongTextPrompt longText) {
        this.resolver = resolver;
        this.workspaces = workspaces;
        this.printer = printer;
        this.reader = reader;
        this.longText = longText;
    }

    @Override
    public void run() {
        var userId = resolver.actingUserId(user);
        boolean interactive = resolver.interactive(noInteractive);
        String codeName = name;
        if (codeName == null) {
            if (!interactive) {
                throw new IllegalArgumentException("Supply --name to create a workspace.");
            }
            codeName = resolver.promptName("Workspace name: ", false);
        } else {
            resolver.validateName(codeName, "workspace name");
        }
        resolver.validateDescription(description);

        String displayTitle = title;
        String details = description;
        if (interactive && displayTitle == null) {
            displayTitle = LinePrompt.read(reader, "Title (Enter to skip): ");
            if (displayTitle.isEmpty()) {
                displayTitle = null;
            }
        }
        if (interactive && details == null) {
            details = longText.read("Description: ", "", Workspace.DESCRIPTION_LIMIT);
            if (details.isEmpty()) {
                details = null;
            }
        }

        printer.print(workspaces.create(userId, WorkspaceCreate.builder()
                .name(codeName).title(displayTitle).description(details).build()));
    }
}
