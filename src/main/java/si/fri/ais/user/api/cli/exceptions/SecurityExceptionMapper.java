package si.fri.ais.user.api.cli.exceptions;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.exceptions.CliExceptionMapper;

@JBossLog
@IfBuildProfile("cli")
@ApplicationScoped
public class SecurityExceptionMapper implements CliExceptionMapper {

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof SecurityException;
    }

    @Override
    public int handle(Throwable exception, CommandLine command, CommandLine.ParseResult parseResult) {
        log.debug("User command failed", exception);
        command.getErr().println("Error: Authentication failed. Check the user name and password.");
        return CommandLine.ExitCode.SOFTWARE;
    }
}
