package si.fri.ais.user.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserCliAction;
import si.fri.ais.user.core.ports.outgoing.UserAuthnService;

@Dependent
@CommandLine.Command(name = "logout", description = "Log out a user", mixinStandardHelpOptions = true)
public class UserLogoutCliAction implements UserCliAction {

    private final UserAuthnService service;
    private final UserNameResolver userNameResolver;

    @CommandLine.Option(names = {"-n", "--name"}, description = "User name (defaults to the session user)")
    private String userName;

    public UserLogoutCliAction(UserAuthnService service, UserNameResolver userNameResolver) {
        this.service = service;
        this.userNameResolver = userNameResolver;
    }

    @Override
    public void run() {
        service.logout(userNameResolver.resolveUserName(userName));
    }
}
