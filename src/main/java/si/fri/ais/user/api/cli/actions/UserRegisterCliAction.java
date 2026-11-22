package si.fri.ais.user.api.cli.actions;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.Dependent;
import org.jline.reader.LineReader;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserCliAction;
import si.fri.ais.user.core.models.commands.UserCreate;
import si.fri.ais.user.core.ports.outgoing.UserService;

import static si.fri.ais.common.core.utils.MainApplicationValues.MAX_PASSWORD_LENGTH;
import static si.fri.ais.common.core.utils.MainApplicationValues.MIN_PASSWORD_LENGTH;

@IfBuildProfile("cli")
@Dependent
@CommandLine.Command(
        name = "create",
        description = "Create a new user",
        mixinStandardHelpOptions = true
)
public class UserRegisterCliAction implements UserCliAction {

    private final LineReader lineReader;
    private final UserService userService;

    @CommandLine.Option(names = {"-n", "--name"}, required = true, description = "New user's name")
    String name;

    @CommandLine.Option(names = {"-p", "--password"}, arity = "0..1", description = "Password")
    char[] password;

    public UserRegisterCliAction(LineReader lineReader, UserService userService) {
        this.lineReader = lineReader;
        this.userService = userService;
    }

    @Override
    public void run() {
        lineReader.setVariable(LineReader.DISABLE_HISTORY, Boolean.TRUE);
        String password;

        try {
            while (true) {
                String input = lineReader.readLine("Insert user password:", '*');

                if (input.length() < MIN_PASSWORD_LENGTH) {
                    lineReader.printAbove("Password is shorter then minimum length of %d characters. Try again.%n".formatted(MIN_PASSWORD_LENGTH));
                    continue;
                }

                if (input.length() > MAX_PASSWORD_LENGTH) {
                    lineReader.printAbove("Password exceeds maximum length of %d characters. Try again.%n".formatted(MAX_PASSWORD_LENGTH));
                    continue;
                }

                password = input;
                break;
            }
        } finally {
            lineReader.getTerminal().flush();
            lineReader.setVariable(LineReader.DISABLE_HISTORY, Boolean.FALSE);
        }
        userService.createUser(UserCreate.builder()
                .name(name)
                .password(password.toCharArray())
                .build());
    }
}
