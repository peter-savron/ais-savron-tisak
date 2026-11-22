package si.fri.ais.user.api.cli;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.CliFeature;

@Dependent
@CommandLine.Command(
        name = "user",
        description = "User related operations.",
        mixinStandardHelpOptions = true,
        modelTransformer = UserCliFeature.UserActionsRegistration.class
)
public class UserCliFeature implements CliFeature {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec commandSpec;

    @Override
    public void run() {
        commandSpec.commandLine().usage(System.out);
    }

    @ApplicationScoped
    public static class UserActionsRegistration implements CommandLine.IModelTransformer {
        private final Instance<UserCliAction> actions;

        public UserActionsRegistration(Instance<UserCliAction> actions) {
            this.actions = actions;
        }

        @Override
        public CommandLine.Model.CommandSpec transform(CommandLine.Model.CommandSpec spec) {
            actions.forEach(spec.commandLine()::addSubcommand);
            return spec;
        }
    }
}
