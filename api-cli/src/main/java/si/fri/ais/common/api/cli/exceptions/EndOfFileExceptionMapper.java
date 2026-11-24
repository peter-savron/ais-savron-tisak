package si.fri.ais.common.api.cli.exceptions;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import org.jline.reader.EndOfFileException;
import picocli.CommandLine;

@JBossLog
@ApplicationScoped
public class EndOfFileExceptionMapper implements CliExceptionMapper {

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof EndOfFileException;
    }

    @Override
    public int handle(Throwable exception, CommandLine command, CommandLine.ParseResult parseResult) {
        log.debug("CLI input interrupted", exception);
        command.getErr().println("Error: Input ended before a value was entered.");
        return CommandLine.ExitCode.SOFTWARE;
    }
}
