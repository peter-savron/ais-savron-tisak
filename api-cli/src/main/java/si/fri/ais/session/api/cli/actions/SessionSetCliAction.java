package si.fri.ais.session.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.session.api.cli.SessionCliAction;
import si.fri.ais.session.api.cli.exceptions.SessionCliException;
import si.fri.ais.session.core.models.commands.SessionSet;
import si.fri.ais.session.core.ports.outgoing.SessionService;

@Dependent
@CommandLine.Command(name = "set", description = "Update the preferences of an active session", mixinStandardHelpOptions = true)
public class SessionSetCliAction implements SessionCliAction {
    private final SessionService sessions;
    private final SessionCliResolver resolver;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.ArgGroup
    Selector selector;

    @CommandLine.Option(names = {"-u", "--user"}, description = "User name")
    String user;

    @CommandLine.Option(names = {"-w", "--workspace"}, description = "Workspace name")
    String workspace;

    public SessionSetCliAction(SessionService sessions, SessionCliResolver resolver) {
        this.sessions = sessions;
        this.resolver = resolver;
    }

    @Override
    public void run() {
        var slot = resolver.slot(selector != null && selector.global, selector == null ? null : selector.name);
        resolver.validateWorkspaceOption(workspace);
        var userId = resolver.userId(user, true);
        if (user == null) {
            resolver.promptToKeepWorkspace();
        }
        sessions.set(SessionSet.builder().slot(slot).userId(userId).build());
        spec.commandLine().getOut().printf("Updated session %s.%n", slot.getId());
    }

    static class Selector {
        @CommandLine.Option(names = {"-g", "--global"}, required = true, description = "Select the global session")
        boolean global;

        @CommandLine.Option(names = {"-n", "--name"}, required = true, description = "Select a named session")
        String name;
    }
}
