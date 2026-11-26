package si.fri.ais.session.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.session.api.cli.SessionCliAction;
import si.fri.ais.session.core.models.commands.SessionStart;
import si.fri.ais.session.core.ports.outgoing.SessionService;

@Dependent
@CommandLine.Command(name = "start", description = "Start a terminal, named or global session", mixinStandardHelpOptions = true)
public class SessionStartCliAction implements SessionCliAction {
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

    public SessionStartCliAction(SessionService sessions, SessionCliResolver resolver) {
        this.sessions = sessions;
        this.resolver = resolver;
    }

    @Override
    public void run() {
        var slot = resolver.slot(selector != null && selector.global, selector == null ? null : selector.name);
        resolver.validateWorkspaceOption(workspace);
        sessions.start(SessionStart.builder().slot(slot).userId(resolver.userId(user, false)).build());
        spec.commandLine().getOut().printf("Started %s.%n", SessionCliPrinter.label(slot));
    }

    static class Selector {
        @CommandLine.Option(names = {"-l", "--local"}, required = true, description = "Select the current terminal session (default)")
        boolean local;

        @CommandLine.Option(names = {"-g", "--global"}, required = true, description = "Select the global session")
        boolean global;

        @CommandLine.Option(names = {"-n", "--name"}, required = true, description = "Select a named session")
        String name;
    }
}
