package si.fri.ais.workspace.api.cli;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.CliFeature;

@Dependent
@CommandLine.Command(
        name = "workspace",
        description = "Manage your workspaces.",
        mixinStandardHelpOptions = true,
        modelTransformer = WorkspaceCliFeature.WorkspaceActionsRegistration.class
)
public class WorkspaceCliFeature implements CliFeature {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec commandSpec;

    @Override
    public void run() {
        commandSpec.commandLine().usage(System.out);
    }

    @ApplicationScoped
    public static class WorkspaceActionsRegistration implements CommandLine.IModelTransformer {
        private final Instance<WorkspaceCliAction> actions;

        public WorkspaceActionsRegistration(Instance<WorkspaceCliAction> actions) {
            this.actions = actions;
        }

        @Override
        public CommandLine.Model.CommandSpec transform(CommandLine.Model.CommandSpec spec) {
            actions.forEach(spec.commandLine()::addSubcommand);
            return spec;
        }
    }
}
