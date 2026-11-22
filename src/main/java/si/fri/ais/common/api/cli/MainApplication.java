package si.fri.ais.common.api.cli;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.inject.Instance;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.exceptions.ExceptionMapperResolver;

@QuarkusMain
public class MainApplication implements QuarkusApplication {
    private final CommandLine.IFactory factory;
    private final MainCommand mainCommand;
    private final ExceptionMapperResolver exceptionMapperResolver;
    private final Instance<CliFeature> cliFeatures;

    public MainApplication(CommandLine.IFactory factory, MainCommand mainCommand, ExceptionMapperResolver exceptionMapperResolver, Instance<CliFeature> cliFeatures) {
        this.factory = factory;
        this.mainCommand = mainCommand;
        this.exceptionMapperResolver = exceptionMapperResolver;
        this.cliFeatures = cliFeatures;
    }

    @Override
    public int run(String... args) {
        // Setup command line
        CommandLine cmd = new CommandLine(mainCommand, factory);
        cliFeatures.forEach(feature -> cmd.addSubcommand(new CommandLine(feature, factory)));
        cmd.setExecutionExceptionHandler(exceptionMapperResolver);

        return cmd.execute(args);
    }
}
