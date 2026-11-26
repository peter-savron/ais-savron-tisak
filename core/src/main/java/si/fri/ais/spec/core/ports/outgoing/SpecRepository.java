package si.fri.ais.spec.core.ports.outgoing;

import si.fri.ais.spec.core.models.*;
import java.util.*;

/**
 * Reads return detached committed snapshots. Populate potentiallyStale on returned models
 * from the source status read in the same snapshot. The flag describes the read and is not persisted.
 */
public interface SpecRepository {
    /** Find a stored publication by ID, including a retained soft-deleted publication. */
    Optional<RawSpec> get(UUID rawId);

    /** Read the latest visible normalized text. Empty for a missing or hidden publication. */
    Optional<String> content(UUID rawId);

    /** Insert metadata and normalized text atomically while the source is SYNCING. */
    void create(RawSpec publication, String text);

    /** Replace metadata and normalized text in one atomic write while SYNCING. */
    void update(RawSpec publication, String text);

    /**
     * Bulk persist updated RawSpec metadata and their new normalized-content references.
     * All publications must belong to the same source and are stored atomically while SYNCING.
     */
    void update(List<RawSpec> publications);

    /**
     * Update publication metadata while the source is SYNCING.
     * A deleted publication loses visibility and retains the last live revision for a later sync to compare and reactivate.
     */
    void update(RawSpec publication);

    /** Permanently remove the publication and its retained revisions during source teardown. */
    void delete(UUID rawId);

    /** Remove a source's managed files, publications and revision history. Safe to retry. */
    void deletePublications(UUID sourceId);

    /** Remove a source's original observations after publication cleanup. Safe to retry. */
    void deleteOriginals(UUID sourceId);

    List<OriginalSpec> items(UUID sourceId);
    /**
     * List publication metadata for a source. Include retained soft-deleted records
     * when requested. Each model carries its deletion and staleness flags.
     */
    List<RawSpec> publications(UUID sourceId, boolean includeDeleted);
    /** Hide tombstones and publications under a marked source for all ordinary reads. */
    Optional<RawSpec> published(UUID rawId);
    /** Return visible publication history in revision order, with IDs usable for diff and publication timestamps. */
    Optional<List<RawSpecRevision>> getRevisions(UUID rawId);
    /** Compare two published versions selected by opaque revision ID and report their publication times. */
    Optional<SpecDiff> diff(UUID rawId, String base, String target);
    /** Persist the upstream observation while retaining its current publication, if any. */
    void update(OriginalSpec original);

}
