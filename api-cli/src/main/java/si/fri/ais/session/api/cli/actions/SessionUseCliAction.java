package si.fri.ais.session.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.session.api.cli.SessionCliAction;
import si.fri.ais.session.api.cli.exceptions.SessionCliException;
import si.fri.ais.session.core.exceptions.SessionException;
import si.fri.ais.session.core.models.Session;
import si.fri.ais.session.core.models.SessionSlot;
import si.fri.ais.session.core.models.commands.SessionUse;
import si.fri.ais.session.core.ports.outgoing.SessionService;

@Dependent
@CommandLine.Command(name = "use", description = "Select the local, global or a named session", mixinStandardHelpOptions = true)
public class SessionUseCliAction implements SessionCliAction {
    private final SessionService sessions;
    private final SessionCliResolver resolver;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.ArgGroup
    Selector selector;

    public SessionUseCliAction(SessionService sessions, SessionCliResolver resolver) {
        this.sessions = sessions;
        this.resolver = resolver;
    }

    @Override
    public void run() {
        SessionSlot terminal = resolver.slot(false, null);
        if (selector == null) {
            resolver.requireInteractive(SessionCliException.Code.SESSION_TARGET_REQUIRED);
            while (true) {
                try {
                    select(terminal, resolver.chooseNamedSession());
                    return;
                } catch (SessionException e) {
                    if (e.getCode() != SessionException.Code.LINKED_SESSION_INACTIVE
                            && e.getCode() != SessionException.Code.LOGIN_REQUIRED
                            && e.getCode() != SessionException.Code.USER_MISSING) {
                        throw e;
                    }
                    resolver.reportSelectionChanged();
                }
            }
        }

        if (selector.local) {
            select(terminal, null);
        } else {
            select(terminal, selector.global ? SessionSlot.GLOBAL_ID : resolver.sessionName(selector.name));
        }
    }

    private void select(SessionSlot terminal, String target) {
        Session selected = sessions.use(SessionUse.builder().terminal(terminal).targetId(target).build())
                .getEffectiveSession();
        spec.commandLine().getOut().printf("Using %s.%n", SessionCliPrinter.label(selected.getSlot()));
    }

    static class Selector {
        @CommandLine.Option(names = {"-l", "--local"}, required = true, description = "Use the terminal's own session")
        boolean local;

        @CommandLine.Option(names = {"-g", "--global"}, required = true, description = "Use the global session")
        boolean global;

        @CommandLine.Option(names = {"-n", "--name"}, required = true, description = "Use a named session")
        String name;
    }
}
