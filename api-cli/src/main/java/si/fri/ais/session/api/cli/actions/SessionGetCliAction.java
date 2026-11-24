package si.fri.ais.session.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.session.api.cli.SessionCliAction;
import si.fri.ais.session.api.cli.exceptions.SessionCliException;
import si.fri.ais.session.core.models.commands.SessionGet;
import si.fri.ais.session.core.ports.outgoing.SessionService;

@Dependent
@CommandLine.Command(name = "get", description = "Show an active session and its linked session", mixinStandardHelpOptions = true)
public class SessionGetCliAction implements SessionCliAction {
    private final SessionService sessions;
    private final SessionCliResolver resolver;
    private final SessionCliPrinter printer;

    @CommandLine.ArgGroup
    Selector selector;

    public SessionGetCliAction(SessionService sessions, SessionCliResolver resolver, SessionCliPrinter printer) {
        this.sessions = sessions;
        this.resolver = resolver;
        this.printer = printer;
    }

    @Override
    public void run() {
        var slot = resolver.slot(selector != null && selector.global, selector == null ? null : selector.name);
        var result = sessions.get(SessionGet.builder().slot(slot).build());
        if (selector == null && result.isEmpty()) {
            slot = resolver.slot(true, null);
            result = sessions.get(SessionGet.builder().slot(slot).build());
        }
        if (result.isEmpty()) {
            throw new SessionCliException(SessionCliException.Code.SESSION_INACTIVE, slot.getId());
        }
        printer.print(result.get());
    }

    static class Selector {
        @CommandLine.Option(names = {"-g", "--global"}, required = true, description = "Select the global session")
        boolean global;

        @CommandLine.Option(names = {"-n", "--name"}, required = true, description = "Select a named session")
        String name;
    }
}
