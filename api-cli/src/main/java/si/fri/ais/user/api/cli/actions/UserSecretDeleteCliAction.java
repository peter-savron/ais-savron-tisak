package si.fri.ais.user.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserSecretCliAction;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.ports.outgoing.UserSecretService;

@Dependent
@CommandLine.Command(name = "delete", description = "Delete a secret without decrypting its value", mixinStandardHelpOptions = true)
public class UserSecretDeleteCliAction implements UserSecretCliAction {
    private final UserSecretCliResolver resolver;
    private final UserSecretService secrets;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Secret name (prompts if omitted)")
    private String name;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Logged-in owner (defaults to the session user)")
    private String user;

    public UserSecretDeleteCliAction(UserSecretCliResolver resolver, UserSecretService secrets) {
        this.resolver = resolver;
        this.secrets = secrets;
    }

    @Override
    public void run() {
        String secretName = resolver.name(name);
        User owner = resolver.user(user);
        secrets.deleteSecret(resolver.context(owner.getId(), secretName));
        spec.commandLine().getOut().printf("Deleted secret %s.%n", secretName);
    }
}
