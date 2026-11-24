package si.fri.ais.session.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.session.api.cli.SessionCliAction;
import si.fri.ais.session.api.cli.exceptions.SessionCliException;
import si.fri.ais.session.core.exceptions.SessionException;
import si.fri.ais.session.core.models.commands.SessionDelete;
import si.fri.ais.session.core.models.commands.SessionStop;
import si.fri.ais.session.core.ports.outgoing.SessionService;

@Dependent
@CommandLine.Command(name = "delete", description = "Delete a global or named session", mixinStandardHelpOptions = true)
public class SessionDeleteCliAction implements SessionCliAction {
    private final SessionService sessions;
    private final SessionCliResolver resolver;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.ArgGroup
    Selector selector;

    public SessionDeleteCliAction(SessionService sessions, SessionCliResolver resolver) {
        this.sessions = sessions;
        this.resolver = resolver;
    }

    @Override
    public void run() {
        if (selector != null) {
            if (selector.global) {
                var slot = resolver.slot(true, null);
                sessions.stop(SessionStop.builder().slot(slot).build());
                spec.commandLine().getOut().printf("Deleted session %s.%n", slot.getId());
            } else {
                delete(resolver.sessionName(selector.name));
            }
            return;
        }

        resolver.requireInteractive(SessionCliException.Code.SESSION_NAME_REQUIRED);
        while (true) {
            String selected = resolver.chooseNamedSession();
            try {
                delete(selected);
                return;
            } catch (SessionException e) {
                if (e.getCode() != SessionException.Code.NAMED_SESSION_MISSING) {
                    throw e;
                }
                resolver.reportSelectionChanged();
            }
        }
    }

    private void delete(String selected) {
        sessions.delete(SessionDelete.builder().name(selected).build());
        spec.commandLine().getOut().printf("Deleted session %s.%n", selected);
    }

    static class Selector {
        @CommandLine.Option(names = {"-g", "--global"}, required = true, description = "Select the global session")
        boolean global;

        @CommandLine.Option(names = {"-n", "--name"}, required = true, description = "Select a named session")
        String name;
    }
}
