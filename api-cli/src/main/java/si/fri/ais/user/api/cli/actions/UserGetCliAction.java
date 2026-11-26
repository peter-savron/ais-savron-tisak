package si.fri.ais.user.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserCliAction;
import si.fri.ais.user.core.ports.outgoing.UserService;

import java.util.NoSuchElementException;
import java.util.UUID;

@Dependent
@CommandLine.Command(name = "get", description = "Show the session user or a user selected by name or ID", mixinStandardHelpOptions = true)
public class UserGetCliAction implements UserCliAction {

    private final UserService service;
    private final UserNameResolver userNameResolver;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.ArgGroup
    Lookup lookup;

    public UserGetCliAction(UserService service, UserNameResolver userNameResolver) {
        this.service = service;
        this.userNameResolver = userNameResolver;
    }

    @Override
    public void run() {
        var user = (lookup != null && lookup.id != null
                ? service.getUser(lookup.id)
                : service.getUser(userNameResolver.resolveUserName(lookup == null ? null : lookup.name)))
                .orElseThrow(() -> new NoSuchElementException("User not found."));
        spec.commandLine().getOut().println(user.getName());
    }

    static class Lookup {
        @CommandLine.Option(names = "--id", required = true, description = "User ID")
        UUID id;

        @CommandLine.Option(names = {"-n", "--name"}, required = true, description = "User name")
        String name;
    }
}
