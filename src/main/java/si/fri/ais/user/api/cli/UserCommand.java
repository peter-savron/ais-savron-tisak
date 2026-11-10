package si.fri.ais.user.api.cli;

import jakarta.enterprise.context.ApplicationScoped;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.CliFeature;

@ApplicationScoped
@CommandLine.Command(
        name = "user",
        description = "User related operations.",
        mixinStandardHelpOptions = true
)
public class UserCommand implements CliFeature {

    @Override
    public void run() {
        System.out.println("User command invoked.");
    }
}
