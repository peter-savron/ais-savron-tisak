package si.fri.ais.common.api.cli;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.inject.Instance;
import picocli.CommandLine;

@QuarkusMain
public class MainApplication implements QuarkusApplication {
    private final CommandLine.IFactory factory;
    private final MainCommand mainCommand;
    private final Instance<CliFeature> cliFeatures;

    public MainApplication(CommandLine.IFactory factory, MainCommand mainCommand, Instance<CliFeature> cliFeatures) {
        this.factory = factory;
        this.mainCommand = mainCommand;
        this.cliFeatures = cliFeatures;
    }

    @Override
    public int run(String... args) {
        // Setup command line
        CommandLine cmd = new CommandLine(mainCommand, factory);
        cliFeatures.stream().forEach(cmd::addSubcommand);

        return cmd.execute(args);
    }
}
