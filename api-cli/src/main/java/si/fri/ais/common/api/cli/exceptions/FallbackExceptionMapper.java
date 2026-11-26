package si.fri.ais.common.api.cli.exceptions;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

import static java.util.Optional.ofNullable;

@JBossLog
@ApplicationScoped
public class FallbackExceptionMapper implements CliExceptionMapper {

    private final Terminal terminal;

    public FallbackExceptionMapper(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof Exception;
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int handle(Throwable exception, CommandLine cmd, ParseResult parseResult) {

        String message = ofNullable(exception.getMessage()).orElse(exception.getClass().getSimpleName());
        log.error("Command execution failed.", exception);

        String formatted = new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold())
                .append("Error: ")
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
                .append(message)
                .toAnsi(terminal);

        terminal.writer().println(formatted);
        terminal.flush();

        return CommandLine.ExitCode.SOFTWARE;
    }
}
