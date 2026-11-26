package si.fri.ais.spec.core.exceptions;

import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Invalid source configuration, with a field path and explanation for each violation. */
public class SpecSourceValidationException extends SpecSourceException {
    /** A model property path, such as selectionRules[2], and the reason its value is invalid. */
    public record Violation(String propertyPath, String message) {
        public Violation {
            Objects.requireNonNull(propertyPath);
            Objects.requireNonNull(message);
        }
    }

    @Getter
    private final List<Violation> violations;

    public SpecSourceValidationException(String propertyPath, String message) {
        this(List.of(new Violation(propertyPath, message)));
    }

    public SpecSourceValidationException(List<Violation> violations) {
        this(Code.INVALID_CONFIGURATION, violations);
    }

    public SpecSourceValidationException(Code code, List<Violation> violations) {
        super(code, violations.stream().map(v -> v.propertyPath() + ": " + v.message())
                .collect(Collectors.joining(", ")));
        if (violations.isEmpty()) {
            throw new IllegalArgumentException("At least one field violation is required");
        }
        this.violations = List.copyOf(violations);
    }
}
