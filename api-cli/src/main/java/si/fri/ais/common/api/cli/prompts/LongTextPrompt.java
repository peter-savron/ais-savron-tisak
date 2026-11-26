package si.fri.ais.common.api.cli.prompts;

import jakarta.enterprise.context.ApplicationScoped;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.Reference;
import org.jline.reader.Widget;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

import static org.jline.utils.InfoCmp.Capability.*;

@ApplicationScoped
public class LongTextPrompt {
    private final Terminal terminal;

    public LongTextPrompt(Terminal terminal) {
        this.terminal = terminal;
    }

    public String read(String message, String initialText) {
        return readWithHelp(message, initialText, null);
    }

    public String read(String message, String initialText, int limit) {
        if (limit < 0 || initialText.length() > limit) {
            throw new IllegalArgumentException("The initial text exceeds the character limit.");
        }
        return readWithHelp(message, initialText, limit);
    }

    private String readWithHelp(String message, String initialText, Integer limit) {
        terminal.writer().println("Enter adds a new line. Ctrl+D or Ctrl+Enter submits. Ctrl+C cancels.");
        terminal.writer().println("Arrow keys move the cursor. Home or Ctrl+A moves to the start of the line.");
        terminal.writer().println("End or Ctrl+E moves to the end. Backspace and Delete remove characters.");
        terminal.flush();
        return new Editor(terminal, limit).readLine(message, null, initialText);
    }

    private static class Editor extends LineReaderImpl {
        private static final String CTRL_ENTER = "\033[13;5u";
        private static final String CTRL_D = KeyMap.ctrl('D');

        private final Integer limit;

        Editor(Terminal terminal, Integer limit) {
            super(terminal, "bistra", null);
            setVariable(DISABLE_HISTORY, true);
            setVariable(SECONDARY_PROMPT_PATTERN, "");
            this.limit = limit;
            option(Option.DISABLE_EVENT_EXPANSION, true);
            option(Option.KITTY_KEYBOARD, true);

            var keys = new KeyMap<Binding>();
            // Insert Unicode text.
            keys.setUnicode(new Reference(SELF_INSERT));
            // Treat unbound key sequences as undefined.
            keys.setNomatch(new Reference(UNDEFINED_KEY));
            // Insert printable ASCII characters and Tab.
            keys.bind(new Reference(SELF_INSERT), KeyMap.range(" -~"));
            keys.bind(new Reference(SELF_INSERT), "\t");
            // Insert bracketed paste as text.
            keys.bind(new Reference(BEGIN_PASTE), "\033[200~");
            // Enter inserts a newline into the text.
            keys.bind(new Reference("insert-newline"), "\r", "\n");
            // Ctrl+Enter submits via Kitty/CSI-u or xterm modifyOtherKeys.
            // Esc followed by Enter and Ctrl+D also submit.
            keys.bind(new Reference("confirm-text"), CTRL_ENTER, "\033[27;5;13~", "\033\r", "\033\n", CTRL_D);
            // Ctrl+C cancels editing.
            keys.bind(new Reference(SEND_BREAK), KeyMap.ctrl('C'));
            // Left and Right move one character.
            keys.bind(new Reference(BACKWARD_CHAR), KeyMap.key(terminal, key_left), "\033[D");
            keys.bind(new Reference(FORWARD_CHAR), KeyMap.key(terminal, key_right), "\033[C");
            // Up and Down move between lines.
            keys.bind(new Reference(UP_LINE), KeyMap.key(terminal, key_up), "\033[A");
            keys.bind(new Reference(DOWN_LINE), KeyMap.key(terminal, key_down), "\033[B");
            // Home or Ctrl+A moves to the start of the current line.
            keys.bind(new Reference(BEGINNING_OF_LINE), KeyMap.key(terminal, key_home), "\033[H", KeyMap.ctrl('A'));
            // End or Ctrl+E moves to the end of the current line.
            keys.bind(new Reference(END_OF_LINE), KeyMap.key(terminal, key_end), "\033[F", KeyMap.ctrl('E'));
            // Backspace deletes before the cursor. Delete removes the character under it.
            keys.bind(new Reference(BACKWARD_DELETE_CHAR), "\177", "\b");
            keys.bind(new Reference(DELETE_CHAR), KeyMap.key(terminal, key_dc), "\033[3~");
            getKeyMaps().put(MAIN, keys);

            getWidgets().put(SELF_INSERT, limited(getBuiltinWidgets().get(SELF_INSERT)));
            getWidgets().put(BEGIN_PASTE, limited(getBuiltinWidgets().get(BEGIN_PASTE)));
            getWidgets().put("insert-newline", limited(() -> {
                getBuffer().write('\n');
                return true;
            }));
            getWidgets().put("confirm-text", () -> {
                state = State.DONE;
                return true;
            });
        }

        @Override
        public String getLastBinding() {
            String binding = super.getLastBinding();
            // JLine checks the raw EOF key before dispatching widgets on an empty buffer.
            return CTRL_D.equals(binding) ? CTRL_ENTER : binding;
        }

        private Widget limited(Widget edit) {
            if (limit == null) {
                return edit;
            }
            return () -> {
                var before = getBuffer().copy();
                boolean changed = edit.apply();
                if (getBuffer().toString().length() <= limit) {
                    return changed;
                }
                getBuffer().copyFrom(before);
                return false;
            };
        }

        @Override
        protected void redisplay(boolean flush) {
            post = () -> {
                int count = getBuffer().toString().length();
                return new AttributedString(limit == null
                        ? Integer.toString(count) : "%d / %d".formatted(count, limit));
            };
            super.redisplay(flush);
        }
    }
}
