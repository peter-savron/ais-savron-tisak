package si.fri.ais.common.api.cli.providers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import lombok.extern.jbosslog.JBossLog;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

@JBossLog
@ApplicationScoped
public class JLineProvider {

    @Produces
    @Singleton
    public Terminal init() throws IOException {
        return TerminalBuilder.builder()
                .system(true)
                .build();
    }

    public void close(@Disposes Terminal terminal) {
        try {
            terminal.close();
        } catch (IOException e) {
            log.error("Could not close the Terminal.", e);
            throw new RuntimeException(e);
        }
    }

    @Produces
    @Singleton
    public LineReader lineReader(Terminal terminal) {
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
    }
}