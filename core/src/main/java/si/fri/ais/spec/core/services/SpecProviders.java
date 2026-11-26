package si.fri.ais.spec.core.services;

import si.fri.ais.spec.core.exceptions.SpecSourceException;
import si.fri.ais.spec.core.exceptions.SpecSourceException.Code;
import si.fri.ais.spec.core.exceptions.SpecSourceValidationException;
import si.fri.ais.spec.core.exceptions.SpecSourceValidationException.Violation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.inject.Inject;
import si.fri.ais.spec.core.ports.outgoing.SpecProvider;

import java.util.List;
import java.util.Objects;

/** Resolves CDI providers by their declared source type. */
@ApplicationScoped
class SpecProviders {
    private final Instance<SpecProvider> providers;

    @Inject
    SpecProviders(@Any Instance<SpecProvider> providers) {
        this.providers = providers;
    }

    List<String> types() {
        return providers.handlesStream()
                .map(handle -> handle.getBean().getName())
                .filter(Objects::nonNull)
                .sorted()
                .toList();
    }

    SpecProvider get(String type) {
        var selected = providers.select(NamedLiteral.of(type));
        if (selected.isUnsatisfied()) {
            throw new SpecSourceValidationException(Code.UNSUPPORTED_SOURCE_TYPE,
                    List.of(new Violation("type", "Unsupported source type " + type)));
        }
        if (selected.isAmbiguous()) {
            throw new SpecSourceException(Code.AMBIGUOUS_PROVIDER, "Multiple providers for source type " + type);
        }
        return selected.get();
    }
}
