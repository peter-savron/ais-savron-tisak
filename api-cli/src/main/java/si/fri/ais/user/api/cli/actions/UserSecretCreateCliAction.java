package si.fri.ais.user.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserSecretCliAction;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.UserSecret;
import si.fri.ais.user.core.models.commands.UserSecretCreate;
import si.fri.ais.user.core.ports.outgoing.UserSecretService;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

@Dependent
@CommandLine.Command(name = "create", description = "Create an encrypted secret; prompts for a masked value by default", mixinStandardHelpOptions = true)
public class UserSecretCreateCliAction implements UserSecretCliAction {
    private final UserSecretCliResolver resolver;
    private final UserSecretService secrets;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Secret name (prompts if omitted)")
    private String name;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Logged-in owner (defaults to the session user)")
    private String user;

    @CommandLine.Option(names = {"-t", "--type"}, defaultValue = "TEXT",
            description = "Value type: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}); text and numbers use UTF-8")
    private UserSecret.Type type;

    @CommandLine.Option(names = "--value-file", description = "Read the raw value from a file; required for BYTES or without a terminal")
    private Path valueFile;

    public UserSecretCreateCliAction(UserSecretCliResolver resolver, UserSecretService secrets) {
        this.resolver = resolver;
        this.secrets = secrets;
    }

    @Override
    public void run() {
        byte[] value = null;
        try {
            String secretName = resolver.name(name);
            User owner = resolver.user(user);
            value = resolver.value(valueFile, type);
            secrets.createSecret(UserSecretCreate.builder().userId(owner.getId()).secretName(secretName)
                    .type(type).secret(value).build());
            spec.commandLine().getOut().printf("Created secret %s.%n", secretName);
        } finally {
            Optional.ofNullable(value).ifPresent(bytes -> Arrays.fill(bytes, (byte) 0));
        }
    }
}
