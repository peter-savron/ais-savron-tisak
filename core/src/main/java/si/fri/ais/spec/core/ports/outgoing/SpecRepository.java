package si.fri.ais.spec.core.ports.outgoing;

import lombok.Builder;
import si.fri.ais.spec.core.models.*;
import java.io.InputStream;
import java.util.*;

/**
 * Reads return detached committed snapshots. Populate potentiallyStale on returned models
 * from the source status read in the same snapshot. The flag describes the read and is not persisted.
 */
public interface SpecRepository {
    /** Find an item by its stable ID, including unpublished and soft-deleted items. */
    Optional<Spec> get(UUID rawId);

    /** Open normalized UTF-8 content. The caller closes the stream; ignored Specs are omitted unless requested for sync. */
    Optional<InputStream> content(UUID rawId, boolean includeIgnored);

    /** Save the item and publish normalized content atomically while its source is SYNCING.
     * Consume the stream before returning. The caller owns and closes it. */
    void update(Spec spec, InputStream content);

    /** Permanently remove the publication and its retained revisions during source teardown. */
    void delete(UUID rawId);

    /** Remove a source's managed files, item records and revision history. Safe to retry. */
    void deleteSourceSpecs(UUID sourceId);

    /** List non-ignored items for the source, including unavailable and soft-deleted items. */
    List<Spec> items(UUID sourceId);

    /** Read only the internal ID, external ID, retrieved-content hash and upstream hash for each stored item. */
    List<SpecIdentifiers> identifiers(UUID sourceId);

    /** Return items with requested processing statuses, including ignored Specs for synchronization. */
    List<Spec> queryByProcessingStatus(ProcessingStatusQuery query);

    /** Source and processing statuses used to filter stored Specs. */
    @Builder
    record ProcessingStatusQuery(UUID specSourceId, List<Spec.ProcessingStatus> processingStatuses) {
        public ProcessingStatusQuery {
            processingStatuses = List.copyOf(processingStatuses);
        }
    }
    /**
     * List publication metadata for a source. Include retained soft-deleted records
     * when requested. Each model carries its deletion and staleness flags.
     */
    /** List non-ignored publications, optionally including soft-deleted records. */
    List<Spec> publications(UUID sourceId, boolean includeDeleted);
    /** Return a published, non-ignored item whose publication is visible. */
    Optional<Spec> published(UUID rawId);
    /** Return visible, non-ignored publication history in revision order, with IDs and timestamps. */
    Optional<List<RawSpecRevision>> getRevisions(UUID rawId);
    /** Compare two visible, non-ignored published versions selected by opaque revision ID. */
    Optional<SpecDiff> diff(UUID rawId, String base, String target);
    /** Save an item and its visibility, retaining its stored text and revision history. */
    void update(Spec spec);

}
