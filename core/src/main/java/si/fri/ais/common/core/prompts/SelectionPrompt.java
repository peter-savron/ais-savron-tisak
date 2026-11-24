package si.fri.ais.common.core.prompts;

import java.util.List;
import java.util.function.Function;

public interface SelectionPrompt {
    /** Selects one of the supplied options and returns the selected option. */
    <T> T select(List<T> options, Function<T, String> label);
}
