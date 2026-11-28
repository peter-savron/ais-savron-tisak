package si.fri.ais.user.api.cli;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.CliFeature;

@Dependent
@CommandLine.Command(
        name = "secret",
        description = "Manage encrypted user secrets.",
        mixinStandardHelpOptions = true,
        modelTransformer = UserSecretCliFeature.UserSecretActionsRegistration.class
)
public class UserSecretCliFeature implements CliFeature {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(spec.commandLine().getOut());
    }

    @ApplicationScoped
    public static class UserSecretActionsRegistration implements CommandLine.IModelTransformer {
        private final Instance<UserSecretCliAction> actions;

        public UserSecretActionsRegistration(Instance<UserSecretCliAction> actions) {
            this.actions = actions;
        }

        @Override
        public CommandLine.Model.CommandSpec transform(CommandLine.Model.CommandSpec spec) {
            actions.forEach(spec.commandLine()::addSubcommand);
            return spec;
        }
    }
}
