package si.fri.ais.user.api.cli.actions;

import jakarta.enterprise.context.Dependent;
import picocli.CommandLine;
import si.fri.ais.user.api.cli.UserSecretCliAction;
import si.fri.ais.user.api.cli.exceptions.UserSecretCliException;
import si.fri.ais.user.core.models.User;
import si.fri.ais.user.core.models.UserSecret;
import si.fri.ais.user.core.models.commands.UserSecretGet;
import si.fri.ais.user.core.ports.outgoing.UserSecretService;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static si.fri.ais.user.api.cli.exceptions.UserSecretCliException.Code.OUTPUT_FAILURE;
import static si.fri.ais.user.api.cli.exceptions.UserSecretCliException.Code.SECRET_NOT_FOUND;

@Dependent
@CommandLine.Command(name = "get", description = "Reveal a secret after password verification; BYTES values print as Base64", mixinStandardHelpOptions = true)
public class UserSecretGetCliAction implements UserSecretCliAction {
    private final UserSecretCliResolver resolver;
    private final UserSecretService secrets;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Secret name (prompts if omitted)")
    private String name;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Logged-in owner (defaults to the session user)")
    private String user;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Owner's password (prompts with masked input if omitted)")
    private char[] password;

    public UserSecretGetCliAction(UserSecretCliResolver resolver, UserSecretService secrets) {
        this.resolver = resolver;
        this.secrets = secrets;
    }

    @Override
    public void run() {
        try {
            String secretName = resolver.name(name);
            User owner = resolver.user(user);
            password = resolver.password(password);
            try (UserSecret secret = secrets.getSecret(UserSecretGet.builder()
                    .context(resolver.context(owner.getId(), secretName)).password(password).build())
                    .orElseThrow(() -> new UserSecretCliException(SECRET_NOT_FOUND))) {
                if (secret.getType() == UserSecret.Type.BYTES) {
                    spec.commandLine().getOut().println(Base64.getEncoder().encodeToString(secret.getSecret()));
                } else {
                    CharBuffer text = resolver.text(secret.getSecret());
                    try {
                        spec.commandLine().getOut().append(text).println();
                    } finally {
                        if (text.hasArray()) {
                            Arrays.fill(text.array(), '\0');
                        }
                    }
                }
                if (spec.commandLine().getOut().checkError()) {
                    throw new UserSecretCliException(OUTPUT_FAILURE);
                }
            }
        } finally {
            Optional.ofNullable(password).ifPresent(value -> Arrays.fill(value, '\0'));
            password = null;
        }
    }
}
