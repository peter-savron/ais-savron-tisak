package si.fri.ais.session.api.cli;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.CliFeature;

@Dependent
@CommandLine.Command(
        name = "session",
        description = "Manage sessions.",
        mixinStandardHelpOptions = true,
        modelTransformer = SessionCliFeature.SessionActionsRegistration.class
)
public class SessionCliFeature implements CliFeature {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec commandSpec;

    @Override
    public void run() {
        commandSpec.commandLine().usage(System.out);
    }

    @ApplicationScoped
    public static class SessionActionsRegistration implements CommandLine.IModelTransformer {
        private final Instance<SessionCliAction> actions;

        public SessionActionsRegistration(Instance<SessionCliAction> actions) {
            this.actions = actions;
        }

        @Override
        public CommandLine.Model.CommandSpec transform(CommandLine.Model.CommandSpec spec) {
            actions.forEach(spec.commandLine()::addSubcommand);
            return spec;
        }
    }
}
