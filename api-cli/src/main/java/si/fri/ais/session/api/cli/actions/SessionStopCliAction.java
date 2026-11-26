package si.fri.ais.session.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.session.api.cli.SessionCliAction;
import si.fri.ais.session.core.models.commands.SessionStop;
import si.fri.ais.session.core.ports.outgoing.SessionService;

@Dependent
@CommandLine.Command(name = "stop", description = "Stop a local or named session", mixinStandardHelpOptions = true)
public class SessionStopCliAction implements SessionCliAction {
    private final SessionService sessions;
    private final SessionCliResolver resolver;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Select a named session")
    String name;

    public SessionStopCliAction(SessionService sessions, SessionCliResolver resolver) {
        this.sessions = sessions;
        this.resolver = resolver;
    }

    @Override
    public void run() {
        var slot = resolver.slot(false, name);
        sessions.stop(SessionStop.builder().slot(slot).build());
        spec.commandLine().getOut().printf("Stopped %s.%n", SessionCliPrinter.label(slot));
    }
}
