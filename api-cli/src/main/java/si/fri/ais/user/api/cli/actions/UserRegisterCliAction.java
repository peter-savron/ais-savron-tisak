package si.fri.ais.user.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserCliAction;
import si.fri.ais.user.core.models.commands.UserCreate;
import si.fri.ais.user.core.ports.outgoing.UserService;

import java.util.Arrays;

import static java.util.Optional.ofNullable;

@Dependent
@CommandLine.Command(
        name = "create",
        description = "Create a new user",
        mixinStandardHelpOptions = true
)
public class UserRegisterCliAction implements UserCliAction {

    private final UserPasswordCliResolver passwordResolver;
    private final UserService userService;
    private final UserNameResolver userNameResolver;

    @CommandLine.Option(names = {"-n", "--name"}, description = "New user's name (prompts if omitted)")
    String name;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Password (prompts if omitted)")
    char[] password;

    public UserRegisterCliAction(UserPasswordCliResolver passwordResolver, UserService userService, UserNameResolver userNameResolver) {
        this.passwordResolver = passwordResolver;
        this.userService = userService;
        this.userNameResolver = userNameResolver;
    }

    @Override
    public void run() {
        try {
            String userName = userNameResolver.readUserName(name);
            password = passwordResolver.readPassword(password);
            userService.createUser(UserCreate.builder()
                    .name(userName)
                    .password(password)
                    .build());
        } finally {
            ofNullable(password).ifPresent(value -> Arrays.fill(value, '\0'));
            password = null;
        }
    }
}
