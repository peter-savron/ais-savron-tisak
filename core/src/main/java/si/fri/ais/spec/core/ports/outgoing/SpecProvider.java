package si.fri.ais.spec.core.ports.outgoing;

import si.fri.ais.common.core.utils.files.ResettableFile;
import si.fri.ais.spec.core.exceptions.SpecSourceException;
import si.fri.ais.spec.core.exceptions.SpecSourceValidationException;
import si.fri.ais.spec.core.models.RemoteSpec;
import si.fri.ais.spec.core.models.SpecSource;

import si.fri.ais.spec.core.exceptions.SpecSourceReadException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
     * Applies the current selection and returns a complete scan of upstream items, including
     * unchanged items, move records and deletion records. Mark content changes as MODIFIED and
     * provide movedFrom/movedTo when the provider can identify the previous and current keys.
     * Return the scan only after the entire configured selection was scanned successfully.
     * Throw when any issue prevents a complete result.
     *
     * @param source authorized source configuration
     * @return all remote facts, move records and deletion records, or empty when selection is empty
     * @throws SpecSourceException with code DISCOVERY_FAILED when a complete scan cannot be returned
     */
    List<RemoteSpec> discover(SpecSource source);

    /**
     * Reads the observed version of the item and, when present, returns its content in a stable file.
     * Verify the observed timestamp when available before returning.
     * Return empty when no item exists at the source. Throw a reason-coded read exception for
     * retrieval failures; core marks an empty result or exception as Spec status UNAVAILABLE.
     *
     * @param source authorized source configuration
     * @param externalId source-scoped key used to retrieve the file
     * @param updatedAt expected upstream modification time, or null if unknown
     * @return a result the caller must close after use, or empty when the item is absent
     * @throws SpecSourceReadException if retrieval fails, with the reason code and underlying cause when available
     */
    ResettableFile read(SpecSource source, String externalId, Instant updatedAt) throws SpecSourceReadException;
}
