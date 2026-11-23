package si.fri.ais.user.api.cli.actions;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserCliAction;
import si.fri.ais.user.core.models.commands.UserDelete;
import si.fri.ais.user.core.ports.outgoing.UserService;

import java.util.Arrays;

import static java.util.Optional.ofNullable;

@IfBuildProfile("cli")
@Dependent
@CommandLine.Command(name = "delete", description = "Delete a user", mixinStandardHelpOptions = true)
public class UserDeleteCliAction implements UserCliAction {

    private final UserNameResolver userNameResolver;
    private final UserPasswordCliResolver passwordResolver;
    private final UserService userService;

    @CommandLine.Option(names = {"-n", "--name"}, description = "User name (prompts if omitted)")
    private String userName;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Password (prompts if omitted)")
    private char[] password;

    public UserDeleteCliAction(UserNameResolver userNameResolver, UserPasswordCliResolver passwordResolver, UserService userService) {
        this.userNameResolver = userNameResolver;
        this.passwordResolver = passwordResolver;
        this.userService = userService;
    }

    @Override
    public void run() {
        try {
            String name = userNameResolver.readUserName(userName);
            password = passwordResolver.readPassword(password);
            userService.deleteUser(UserDelete.builder()
                    .name(name)
                    .password(password)
                    .build());
        } finally {
            ofNullable(password).ifPresent(value -> Arrays.fill(value, '\0'));
            password = null;
        }
    }
}
