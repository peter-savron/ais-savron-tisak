package si.fri.ais.user.api.cli.actions;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserCliAction;
import si.fri.ais.user.core.models.commands.UserLogin;
import si.fri.ais.user.core.ports.outgoing.UserAuthnService;

import java.util.Arrays;

import static java.util.Optional.ofNullable;

@IfBuildProfile("cli")
@Dependent
@CommandLine.Command(name = "login", description = "Log in a user", mixinStandardHelpOptions = true)
public class UserLoginCliAction implements UserCliAction {

    private final UserNameResolver userNameResolver;
    private final UserPasswordCliResolver passwordResolver;
    private final UserAuthnService userAuthnService;

    @CommandLine.Option(names = {"-n", "--name"}, description = "User name (prompts if omitted)")
    private String userName;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Password (prompts if omitted)")
    private char[] password;

    public UserLoginCliAction(UserNameResolver userNameResolver, UserPasswordCliResolver passwordResolver, UserAuthnService userAuthnService) {
        this.userNameResolver = userNameResolver;
        this.passwordResolver = passwordResolver;
        this.userAuthnService = userAuthnService;
    }

    @Override
    public void run() {
        try {
            String name = userNameResolver.readUserName(userName);
            password = passwordResolver.readPassword(password);
            userAuthnService.login(UserLogin.builder()
                    .name(name)
                    .password(password)
                    .build());
        } finally {
            ofNullable(password).ifPresent(value -> Arrays.fill(value, '\0'));
            password = null;
        }
    }
}
