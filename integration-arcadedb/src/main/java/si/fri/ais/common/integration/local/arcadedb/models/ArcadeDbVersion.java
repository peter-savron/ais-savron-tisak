package si.fri.ais.common.integration.local.arcadedb.models;

import jakarta.validation.constraints.PositiveOrZero;

import java.util.Comparator;

/**
 * Represents a version identifier for ArcadeDB changelogs, supporting both
 * natural ordering ({@link Comparable}) and standalone comparison ({@link Comparator}).
 *
 * <p><b>Ordering rules:</b> Fields are compared sequentially in the order
 * {@code major} -&gt; {@code minor} -&gt; {@code patch} -&gt; {@code custom}.
 * For each field:
 * <ul>
 *   <li>If one version has {@code null} and the other has a non-null value,
 *       the version with {@code null} comes first (e.g., {@code (1, null, null, null)}
 *       comes before {@code (1, 0, null, null)}).</li>
 *   <li>If both have {@code null}, comparison moves to the next field.</li>
 *   <li>If both have non-null values, standard natural ordering is used
 *       (lesser integer or lexicographically smaller string comes first).</li>
 * </ul>
 *
 * @param major  Major version number (optional, must be &gt;= 0 if present).
 * @param minor  Minor version number (optional, must be &gt;= 0 if present).
 * @param patch  Patch version number (optional, must be &gt;= 0 if present).
 * @param custom Optional custom/pre-release qualifier.
 */
public record ArcadeDbVersion(
        @PositiveOrZero Integer major,
        @PositiveOrZero Integer minor,
        @PositiveOrZero Integer patch,
        String custom
) implements Comparable<ArcadeDbVersion>, Comparator<ArcadeDbVersion> {

    public static final Comparator<ArcadeDbVersion> COMPARATOR = Comparator
            .comparing(ArcadeDbVersion::major, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(ArcadeDbVersion::minor, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(ArcadeDbVersion::patch, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(ArcadeDbVersion::custom, Comparator.nullsFirst(Comparator.naturalOrder()));

    public ArcadeDbVersion {
        if (major != null && major < 0) {
            throw new IllegalArgumentException("major must be >= 0, got: " + major);
        }
        if (minor != null && minor < 0) {
            throw new IllegalArgumentException("minor must be >= 0, got: " + minor);
        }
        if (patch != null && patch < 0) {
            throw new IllegalArgumentException("patch must be >= 0, got: " + patch);
        }
    }

    /**
     * No-arg constructor producing an all-null version, useful for instantiating as a {@link Comparator}.
     */
    public ArcadeDbVersion() {
        this(null, null, null, null);
    }

    /**
     * Parses a version string such as {@code "1"}, {@code "1.2"}, {@code "1.2.3"},
     * or {@code "1.2.3-alpha"}. Missing numeric segments remain {@code null}.
     */
    public static ArcadeDbVersion of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Version string cannot be blank");
        }
        String[] splitCustom = raw.trim().split("-", 2);
        String[] nums = splitCustom[0].split("\\.");

        if (nums.length > 3) {
            throw new IllegalArgumentException("Expected at most 3 numeric segments, got: " + raw);
        }

        return new ArcadeDbVersion(
                nums.length > 0 && !nums[0].isEmpty() ? Integer.parseInt(nums[0]) : null,
                nums.length > 1 && !nums[1].isEmpty() ? Integer.parseInt(nums[1]) : null,
                nums.length > 2 && !nums[1].isEmpty() ? Integer.parseInt(nums[2]) : null,
                splitCustom.length > 1 ? splitCustom[1] : null
        );
    }

    @Override
    public int compareTo(ArcadeDbVersion other) {
        return COMPARATOR.compare(this, other);
    }

    @Override
    public int compare(ArcadeDbVersion v1, ArcadeDbVersion v2) {
        return COMPARATOR.compare(v1, v2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (major != null) sb.append(major);
        if (minor != null) sb.append('.').append(minor);
        if (patch != null) sb.append('.').append(patch);
        if (custom != null && !custom.isBlank()) {
            if (!sb.isEmpty()) sb.append('-');
            sb.append(custom);
        }
        return sb.toString();
    }
}