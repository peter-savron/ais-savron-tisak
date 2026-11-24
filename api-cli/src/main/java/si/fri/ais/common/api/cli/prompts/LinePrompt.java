package si.fri.ais.common.api.cli.prompts;

import org.jline.reader.LineReader;

public final class LinePrompt {

    private LinePrompt() {
    }

    public static String read(LineReader lineReader, String message) {
        return lineReader.readLine(message);
    }
}
