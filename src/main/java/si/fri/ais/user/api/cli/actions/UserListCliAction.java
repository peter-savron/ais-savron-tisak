package si.fri.ais.user.api.cli.actions;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserCliAction;
import si.fri.ais.user.core.ports.outgoing.UserService;

@IfBuildProfile("cli")
@Dependent
@CommandLine.Command(name = "list", description = "List users", mixinStandardHelpOptions = true)
public class UserListCliAction implements UserCliAction {

    private final UserService service;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public UserListCliAction(UserService service) {
        this.service = service;
    }

    @Override
    public void run() {
        var out = spec.commandLine().getOut();
        var ansi = spec.commandLine().getColorScheme().ansi();
        service.list().forEach(user -> out.printf("- %s%s%n",
                user.getName(), user.isLogged() ? ansi.string(" @|green LOGGED|@") : ""));
    }
}
