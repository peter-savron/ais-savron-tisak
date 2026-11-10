package si.fri.ais.common.api.cli;

import jakarta.enterprise.context.ApplicationScoped;
import picocli.CommandLine;
import si.fri.ais.common.core.utils.MainApplicationValues;


@ApplicationScoped
@CommandLine.Command(
        name = MainApplicationValues.APP_NAME,
        mixinStandardHelpOptions = true,
        versionProvider = MainCliVersionProvider.class,
        description = "A CLI tool for spec refining and code generation."
)
public class MainCommand {
}
