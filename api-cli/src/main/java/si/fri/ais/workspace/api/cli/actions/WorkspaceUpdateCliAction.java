package si.fri.ais.workspace.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import org.jline.reader.LineReader;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.prompts.LinePrompt;
import si.fri.ais.common.api.cli.prompts.LongTextPrompt;
import si.fri.ais.workspace.api.cli.WorkspaceCliAction;
import si.fri.ais.workspace.core.models.Workspace;
import si.fri.ais.workspace.core.models.WorkspaceContext;
import si.fri.ais.workspace.core.models.commands.WorkspaceUpdate;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;

import java.util.Objects;

@Dependent
@CommandLine.Command(name = "update", description = "Update workspace metadata", mixinStandardHelpOptions = true)
public class WorkspaceUpdateCliAction implements WorkspaceCliAction {
    private final WorkspaceCliResolver resolver;
    private final WorkspaceService workspaces;
    private final WorkspaceCliPrinter printer;
    private final LineReader reader;
    private final LongTextPrompt longText;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace code name")
    private String name;

    @CommandLine.Option(names = "--new-name", description = "New workspace code name")
    private String newName;

    @CommandLine.Option(names = "--title", description = "Workspace title")
    private String title;

    @CommandLine.Option(names = "--description", description = "Workspace description")
    private String description;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Acting logged-in user")
    private String user;

    @CommandLine.Option(names = {"-I", "--no-interactive"}, description = "Disable interactive prompts")
    private boolean noInteractive;

    public WorkspaceUpdateCliAction(WorkspaceCliResolver resolver,
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
        boolean hasEdits = newName != null || title != null || description != null;
        if (!interactive && !hasEdits) {
            throw new IllegalArgumentException("Supply at least one of --new-name, --title, or --description.");
        }
        if (newName != null) {
            resolver.validateName(newName, "new workspace name");
        }
        resolver.validateDescription(description);
        Workspace workspace = resolver.workspace(userId, name, interactive);
        String updatedName = newName;
        String updatedTitle = title;
        String updatedDescription = description;
        if (!hasEdits) {
            updatedName = resolver.promptName("New name (Enter to keep %s): ".formatted(workspace.getName()), true);
            updatedTitle = LinePrompt.read(reader, "New title (Enter to keep): ");
            if (updatedTitle.isEmpty()) {
                updatedTitle = null;
            }
            String currentDescription = Objects.toString(workspace.getDescription(), "");
            updatedDescription = longText.read("Description: ",
                    currentDescription, Workspace.DESCRIPTION_LIMIT);
            if (updatedDescription.equals(currentDescription)) {
                updatedDescription = null;
            }
        }

        if (updatedName == null && updatedTitle == null && updatedDescription == null) {
            printer.print(workspace);
            return;
        }
        var context = new WorkspaceContext(userId, workspace.getId());
        printer.print(workspaces.update(context, WorkspaceUpdate.builder()
                .name(updatedName).title(updatedTitle).description(updatedDescription).build()));
    }
}
