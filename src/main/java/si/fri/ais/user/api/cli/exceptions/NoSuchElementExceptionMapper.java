package si.fri.ais.user.api.cli.exceptions;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import picocli.CommandLine;
import si.fri.ais.common.api.cli.exceptions.CliExceptionMapper;

import java.util.NoSuchElementException;

@JBossLog
@IfBuildProfile("cli")
@ApplicationScoped
public class NoSuchElementExceptionMapper implements CliExceptionMapper {

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof NoSuchElementException;
    }

    @Override
    public int handle(Throwable exception, CommandLine command, CommandLine.ParseResult parseResult) {
        log.debug("User command failed", exception);
        command.getErr().println("Error: Something not found not found.");
        return CommandLine.ExitCode.SOFTWARE;
    }
}
