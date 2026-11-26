# Spec metadata in ArcadeDB

The ArcadeDB schema stores source configuration, observed upstream revisions and publication metadata for [SpecSourceService](../../features/SpecSourceService.md).

| Schema type | Data and lookup |
| --- | --- |
| SpecSource (document) | Store the [source fields](../../features/Spec.md#specsource), workspace UUID and configuration version. Index the source UUID uniquely and enforce workspace-scoped spec name uniqueness. |
| Spec (vertex) | One node per (source ID, external ID) with a unique stable UUID. Store the current observation, processing status, the `ignored` flag (default `false`) and optional published version, including visibility, managed path and revision pointer. |
| Spec observation revision (document) | Store immutable observations keyed by Spec UUID and observation revision. Append on verified changes and retain provenance and selection state. |

When merging existing records, retain the publication UUID for already published items so their revision history and managed file names remain addressable. Use the original observation UUID for items without a publication. Repoint history to the surviving UUID and retire the former relationship edge after verifying the references.

Use the [managed file’s Git history](../filesystem/RawSpec.md#per-file-history) for RawSpec text revisions and comparisons. Keep the metadata needed to resolve a publication to its sync commit, file path and Spec observation revision, including metadata-only publications. Git holds the historical text and storage computes its diff on demand. Ordinary reads hide tombstones. Reserve the source directory identity while retained revisions depend on it.

Once the candidate repository commit is durable, atomically advance the source repository pointer, record the latest sync result for the source, and apply all included item revisions, visibility changes and observation outcomes in one transaction. All included revisions reference the same sync commit. A rejected batch leaves the previous head and publications readable. Acquire SYNCING from ACTIVE conditionally. At publication require SYNCING and return the source to ACTIVE within this transaction, and check the expected repository head, source configuration and observation versions. A failed check preserves the committed state. Read text from the recorded commit and path. Successful synchronization reactivates a retained publication under the same Spec UUID.

For [source deletion](../../features/SpecSourceService.md#source-deletion), store the cleanup phase alongside the lifecycle enum. Advance it conditionally for the same source UUID. Query remaining children by source ID, including tombstones. Remove the source record only after verifying that no owned children remain.

For lexical name/path retrieval and generation-scoped passage embeddings, see [RawSpecIndex](RawSpecIndex.md).

Apply `ignored = false` in repository queries for ordinary Spec item and publication lists, publication lookups, content reads, revision lists and diffs. These repository queries must return no result for ignored Specs. Synchronization queries (`identifiers`, `get`, and processing-status queries) include ignored Specs so they remain tracked and updated without changing the flag. The content comparison used during synchronization explicitly includes ignored Specs; workspace-authorized RawSpec reads do not.
