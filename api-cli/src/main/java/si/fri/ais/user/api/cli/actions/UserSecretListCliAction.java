package si.fri.ais.user.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserSecretCliAction;
import si.fri.ais.user.core.models.UserSecretMetadata;
import si.fri.ais.user.core.ports.outgoing.UserSecretService;

import java.util.List;

@Dependent
@CommandLine.Command(name = "list", description = "List secret names and types without decrypting values", mixinStandardHelpOptions = true)
public class UserSecretListCliAction implements UserSecretCliAction {
    private final UserSecretCliResolver resolver;
    private final UserSecretService secrets;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Logged-in owner (defaults to the session user)")
    private String user;

    public UserSecretListCliAction(UserSecretCliResolver resolver, UserSecretService secrets) {
        this.resolver = resolver;
        this.secrets = secrets;
    }

    @Override
    public void run() {
        List<UserSecretMetadata> entries = secrets.list(resolver.user(user).getId());
        if (entries.isEmpty()) {
            spec.commandLine().getOut().println("No secrets stored for this user.");
            return;
        }
        entries.forEach(secret -> spec.commandLine().getOut().printf("%s\t%s%n", secret.name(), secret.type()));
    }
}
