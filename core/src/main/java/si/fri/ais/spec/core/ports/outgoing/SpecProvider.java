package si.fri.ais.spec.core.ports.outgoing;

import si.fri.ais.spec.core.exceptions.SpecSourceException;
import si.fri.ais.spec.core.exceptions.SpecSourceValidationException;
import si.fri.ais.spec.core.models.OriginalSpec;
import si.fri.ais.spec.core.models.SpecSource;

import si.fri.ais.spec.core.exceptions.SpecSourceReadException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interprets a source configuration, discovers upstream items and reads their content.
 * Register each implementation as a CDI bean with an explicit {@code @Named} value
 * equal to its stable, case-sensitive source type identifier.
 *
 * <p>Core authorizes access before discovery and reads. The provider accesses the upstream
 * source without modifying it. Core assigns item UUIDs, reconciles observations and publishes
 * converted content through the persistence ports.
 */
public interface SpecProvider {
    /**
     * Describes the provider's root and subfolder syntax, selection rules and configuration example.
     *
     * @return a nonempty Markdown guide available without a configured source or upstream connection
     */
    String describeConfiguration();

    /**
     * Validates the source root, optional subfolder and selection rules for this provider.
     *
     * @param source proposed source configuration
     * @throws SpecSourceValidationException with the field path and reason for each detected violation
     */
    void validate(SpecSource source);

    /**
     * Applies the current selection and reports each detected change once. Omit items whose content,
     * provenance and selection state match their known state. Use UNRESOLVED or ACCESS_DENIED
     * for item-level uncertainty. Report known items as missing or excluded only when their
     * absence or exclusion is confirmed. Throw when discovery issues occur.
     *
     * @param source authorized source configuration
     * @param known previously recorded items that can be assessed against the current scope
     * @return changed OriginalSpecs with discovery statuses, or an empty list when no changes were detected
     * @throws SpecSourceException with code DISCOVERY_FAILED when discovery issues occur
     */
    List<OriginalSpec> discover(SpecSource source, List<OriginalSpec> known);

    /**
     * Reads the observed version of the item and returns its content in a stable file.
     * Verify that the retrieved bytes match the discovery reference before returning.
     * Report a version mismatch or an unavailable item as a read failure.
     *
     * @param source authorized source configuration
     * @param observed item identity and last known locator
     * @return a result the caller must close after use
     * @throws SpecSourceReadException if retrieval fails, with the reason code and underlying cause when available
     */
    Read read(SpecSource source, OriginalSpec observed) throws SpecSourceReadException;

    /** A read result that owns any resources needed to keep the retrieved file stable. */
    interface Read extends AutoCloseable {
        /** Returns the non-null file containing the observed version, stable until close. */
        Path file();

        /**
         * Releases provider-owned resources, including any temporary file created for the read.
         *
         * @throws SpecSourceReadException with code CLEANUP_FAILED and the underlying cause if cleanup fails
         */
        @Override
        void close() throws SpecSourceReadException;
    }
}
