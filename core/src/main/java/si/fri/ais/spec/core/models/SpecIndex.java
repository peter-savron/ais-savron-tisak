package si.fri.ais.spec.core.models;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/** Source-scoped index of stored Spec identifiers used during synchronization. */
public final class SpecIndex {
    private final Map<UUID, SpecIdentifiers> byId = new HashMap<>();
    private final Map<String, SpecIdentifiers> byExternalId = new HashMap<>();
    private final Map<String, List<SpecIdentifiers>> byHash = new HashMap<>();
    private final Map<String, List<SpecIdentifiers>> byExternalHash = new HashMap<>();

    public SpecIndex(List<SpecIdentifiers> specs) {
        for (SpecIdentifiers spec : specs) {
            byId.put(spec.id(), spec);
            byExternalId.put(spec.externalId(), spec);
            Optional.ofNullable(spec.hash()).ifPresent(hash ->
                    byHash.computeIfAbsent(hash, key -> new ArrayList<>()).add(spec));
            Optional.ofNullable(spec.externalHash()).ifPresent(hash ->
                    byExternalHash.computeIfAbsent(hash, key -> new ArrayList<>()).add(spec));
        }
        byHash.replaceAll((hash, matches) -> List.copyOf(matches));
        byExternalHash.replaceAll((hash, matches) -> List.copyOf(matches));
    }

    public Optional<SpecIdentifiers> byId(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<SpecIdentifiers> byExternalId(String externalId) {
        return Optional.ofNullable(byExternalId.get(externalId));
    }

    public List<SpecIdentifiers> byHash(String hash) {
        return byHash.getOrDefault(hash, List.of());
    }

    public List<SpecIdentifiers> byExternalHash(String externalHash) {
        return byExternalHash.getOrDefault(externalHash, List.of());
    }

    public Stream<SpecIdentifiers> all() {
        return byId.values().stream();
    }
}
