# Spec metadata in ArcadeDB

The ArcadeDB schema stores source configuration, observed upstream revisions and publication metadata for [SpecSourceService](../../features/SpecSourceService.md).

| Schema type | Data and lookup |
| --- | --- |
| SpecSource (document) | Store the [source fields](../../features/Spec.md#specsource), workspace UUID and configuration version. Index the source UUID uniquely and enforce workspace-scoped spec name uniqueness. |
| OriginalSpec (vertex) | One node per normalized relative path within a source. Enforce a unique index on (source ID, relative path). Keep current observation version, selection state and processing status here. |
| OriginalSpecRevision (document) | Keep immutable observations in ArcadeDB, keyed by source ID, relative path and ordered observation revision. Record the observed provenance, input format, available upstream revision and content hash, plus presence and selection state. Append on a verified change, including exclusion, disappearance or reappearance. Repeated identical observations reuse the revision. Retain references and metadata only. |
| RawSpec (vertex) | One node per published source item with a unique application UUID. Store current [publication metadata](../../features/Spec.md#rawspec), visibility, published repository revision and managed file path. Keep the last live pointer and provenance on soft deletion. |
| DerivedFrom (edge) | Directed from RawSpec to OriginalSpec. Each RawSpec has exactly one outgoing edge, and each OriginalSpec has at most one incoming edge, including when the publication is soft-deleted. |

Create the `DerivedFrom` edge in the same ArcadeDB transaction as the first RawSpec publication. Both vertices must belong to the same source and identify the same normalized relative path. Preserve that edge and both node identities across updates, soft deletion and restoration. The edge identifies the item; the publication records the exact OriginalSpec observation revision it consumed. Delete incident edges with their vertex during ordered source cleanup, and remove observation revisions with their OriginalSpec.

Use the [managed file’s Git history](../filesystem/RawSpec.md#per-file-history) for RawSpec text revisions, comparisons and rollback. Keep the metadata needed to resolve a publication to its sync commit, file path and OriginalSpec revision, including metadata-only publications. Git holds the historical text and storage computes its diff on demand. Ordinary reads hide tombstones. Reserve the source directory identity while retained revisions depend on it.

Once the candidate repository commit is durable, atomically advance the source repository pointer, record the latest sync result for the source, and apply all included item revisions, visibility changes and observation outcomes in one transaction. All included revisions reference the same sync commit. A rejected batch leaves the previous head and publications readable. Acquire SYNCING from ACTIVE conditionally. At publication require SYNCING and restore ACTIVE within this transaction, and check the expected repository head, source configuration and observation versions. A failed check preserves the committed state. Read text from the recorded commit and path. Restoration publishes the retained metadata and file together under the same RawSpec UUID.

For [source deletion](../../features/SpecSourceService.md#source-deletion), store the cleanup phase alongside the lifecycle enum. Advance it conditionally for the same source UUID. Query remaining children by source ID, including tombstones. Remove the source record only after verifying that no owned children remain.

For lexical name/path retrieval and generation-scoped passage embeddings, see [RawSpecIndex](RawSpecIndex.md).
