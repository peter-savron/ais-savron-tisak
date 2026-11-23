package si.fri.ais.common.api.cli.prompts;

import jakarta.enterprise.context.ApplicationScoped;
import org.jline.reader.LineReader;

@ApplicationScoped
public class PasswordPrompt {

    private final LineReader lineReader;

    public PasswordPrompt(LineReader lineReader) {
        this.lineReader = lineReader;
    }

    public char[] read() {
        Object historySetting = lineReader.getVariable(LineReader.DISABLE_HISTORY);
        lineReader.setVariable(LineReader.DISABLE_HISTORY, Boolean.TRUE);
        try {
            return lineReader.readLine("Password: ", '*').toCharArray();
        } finally {
            lineReader.setVariable(LineReader.DISABLE_HISTORY, historySetting);
            lineReader.getTerminal().flush();
        }
    }
}
