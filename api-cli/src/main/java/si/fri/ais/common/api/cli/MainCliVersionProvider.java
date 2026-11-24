package si.fri.ais.common.api.cli;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import picocli.CommandLine;

/**
 * Used to fetch dynamically CLI tool version based on maven project version.
 */
@ApplicationScoped
public class MainCliVersionProvider implements CommandLine.IVersionProvider {
    @ConfigProperty(name = "quarkus.application.version")
    private String appVersion;

    @Override
    public String[] getVersion() {
        return new String[]{appVersion};
    }
}
