package si.fri.ais.workspace.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import org.jline.terminal.Terminal;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.actions.UserPasswordCliResolver;
import si.fri.ais.user.core.ports.outgoing.UserAuthnService;
import si.fri.ais.workspace.api.cli.WorkspaceCliAction;
import si.fri.ais.workspace.core.models.Workspace;
import si.fri.ais.workspace.core.models.WorkspaceContext;
import si.fri.ais.workspace.core.ports.outgoing.WorkspaceService;

import java.util.Arrays;

@Dependent
@CommandLine.Command(name = "delete", description = "Delete a workspace", mixinStandardHelpOptions = true)
public class WorkspaceDeleteCliAction implements WorkspaceCliAction {
    private final WorkspaceCliResolver resolver;
    private final WorkspaceService workspaces;
    private final UserAuthnService authentication;
    private final UserPasswordCliResolver passwords;
    private final Terminal terminal;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace code name")
    private String name;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Acting logged-in user")
    private String user;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Acting user password (prompts if omitted)")
    private char[] password;

    public WorkspaceDeleteCliAction(WorkspaceCliResolver resolver,
                                    WorkspaceService workspaces,
                                    UserAuthnService authentication,
                                    UserPasswordCliResolver passwords,
                                    Terminal terminal) {
        this.resolver = resolver;
        this.workspaces = workspaces;
        this.authentication = authentication;
        this.passwords = passwords;
        this.terminal = terminal;
    }

    @Override
    public void run() {
        try {
            var userId = resolver.actingUserId(user);
            boolean interactive = resolver.interactive(false);
            Workspace workspace = resolver.workspace(userId, name, interactive);
            if (password == null && !interactive) {
                throw new IllegalArgumentException("Supply --password when a terminal is unavailable.");
            }
            password = passwords.readPassword(password, "Password to delete %s: ".formatted(workspace.getName()));
            authentication.verifyPassword(userId, password);
            workspaces.delete(new WorkspaceContext(userId, workspace.getId()));
            terminal.writer().printf("Deleted workspace %s.%n", workspace.getName());
            terminal.flush();
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
                password = null;
            }
        }
    }
}
