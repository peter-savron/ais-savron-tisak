package si.fri.ais.common.api.cli.exceptions;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import org.jline.reader.UserInterruptException;
import picocli.CommandLine;

@JBossLog
@ApplicationScoped
public class UserInterruptExceptionMapper implements CliExceptionMapper {

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof UserInterruptException;
    }

    @Override
    public int handle(Throwable exception, CommandLine command, CommandLine.ParseResult parseResult) {
        log.debug("CLI input interrupted", exception);
        command.getErr().println("Error: Input cancelled.");
        return CommandLine.ExitCode.SOFTWARE;
    }
}
