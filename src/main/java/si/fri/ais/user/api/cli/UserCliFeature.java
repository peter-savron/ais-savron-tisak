package si.fri.ais.user.api.cli;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.CliFeature;

@ApplicationScoped
@CommandLine.Command(
        name = "user",
        description = "User related operations.",
        mixinStandardHelpOptions = true
)
public class UserCliFeature implements CliFeature {
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
