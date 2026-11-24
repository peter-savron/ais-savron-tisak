package si.fri.ais.common.api.cli.prompts;

import jakarta.enterprise.context.ApplicationScoped;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Display;
import si.fri.ais.common.core.prompts.SelectionPrompt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.jline.utils.InfoCmp.Capability.key_down;
import static org.jline.utils.InfoCmp.Capability.key_up;

@ApplicationScoped
public class JLineSelectionPrompt implements SelectionPrompt {
    private enum Key { UP, DOWN, ACCEPT, CANCEL, EOF, IGNORE }

    private final LineReader reader;

    public JLineSelectionPrompt(LineReader reader) {
        this.reader = reader;
    }

    @Override
    public <T> T select(List<T> options, Function<T, String> label) {
        if (options.isEmpty()) {
            throw new IllegalArgumentException("At least one selection option is required.");
        }

        var terminal = reader.getTerminal();
        var input = new BindingReader(terminal.reader());
        var keys = new KeyMap<Key>();
        keys.bind(Key.UP, KeyMap.key(terminal, key_up), "\033[A", "k");
        keys.bind(Key.DOWN, KeyMap.key(terminal, key_down), "\033[B", "j");
        keys.bind(Key.ACCEPT, "\r", "\n");
        keys.bind(Key.CANCEL, KeyMap.ctrl('C'));
        keys.bind(Key.EOF, KeyMap.ctrl('D'));
        keys.setNomatch(Key.IGNORE);
        keys.setUnicode(Key.IGNORE);

        var entries = List.copyOf(options);
        var display = new Display(terminal, false);
        var attributes = terminal.enterRawMode();
        try {
            int selected = 0;
            while (true) {
                display.resize(terminal);
                int visible = Math.max(1, display.getRows() - 2);
                int first = Math.max(0, selected - visible + 1);
                int width = Math.max(1, display.getColumns() - 1);
                var lines = new ArrayList<AttributedString>();
                lines.add(new AttributedString("Use arrow keys to select and Enter to confirm.").columnSubSequence(0, width));
                for (int i = first; i < Math.min(entries.size(), first + visible); i++) {
                    String text = label.apply(entries.get(i)).replaceAll("\\p{Cntrl}", " ");
                    var style = i == selected ? AttributedStyle.DEFAULT.inverse() : AttributedStyle.DEFAULT;
                    lines.add(new AttributedString((i == selected ? "> " : "  ") + text, style)
                            .columnSubSequence(0, width));
                }
                display.update(lines, 0);

                Key key = input.readBinding(keys);
                if (key == null || key == Key.EOF) {
                    throw new EndOfFileException();
                }
                switch (key) {
                    case UP -> selected = Math.max(0, selected - 1);
                    case DOWN -> selected = Math.min(entries.size() - 1, selected + 1);
                    case ACCEPT -> {
                        return entries.get(selected);
                    }
                    case CANCEL -> throw new UserInterruptException("");
                    default -> {
                    }
                }
            }
        } finally {
            terminal.setAttributes(attributes);
            display.update(List.of(), 0);
        }
    }
}
