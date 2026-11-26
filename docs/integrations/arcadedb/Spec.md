# Spec metadata in ArcadeDB

Use documents with indexed identifiers for the [SpecService operations](../../features/SpecService.md). Workspace membership and source provenance use UUID references and require no graph traversal.

| Document | Stored data and lookup |
| --- | --- |
| SpecSource | Store the [source fields](../../features/Spec.md#specsource) and configuration version. Index the source UUID uniquely and enforce workspace-scoped spec name uniqueness. Index by workspace for listing. |
| OriginalSpec | Store the [reference and observation metadata](../../features/Spec.md#originalspec), observation version, selection state, processing status and latest failure reason when applicable. Enforce uniqueness on (source ID, item identity). |
| RawSpec | Store the [RawSpec metadata](../../features/Spec.md#rawspec), including input hash, provenance, output format and brief metadata, plus the relative path of the published text file. Index the RawSpec UUID uniquely and enforce one current publication per (source ID, item identity). The [filesystem](../filesystem/RawSpec.md) holds the normalized text. |

Keep a revision document keyed by (RawSpec UUID, revision ID) with its publication metadata snapshot, repository revision and path. On soft deletion, keep the last live revision, deletion reason and owner-resolvable workspace/source references. Filter tombstones from ordinary reads, including history. Reserve the source directory identity while retained revisions depend on it.

Once the candidate repository commit is durable, atomically advance the source repository pointer, record the sync result keyed uniquely by sync ID, and apply all included item revisions, visibility changes and observation outcomes in one transaction. All included revisions reference the same sync commit. A rejected batch leaves the previous head and publications readable. Require ACTIVE source status and check the expected repository head, source configuration and observation versions. A failed check preserves the committed state. Read text from the recorded commit and path. Restoration publishes the retained metadata and file together under the same RawSpec UUID.

For [source deletion](../../features/SpecService.md#source-deletion), store the cleanup phase alongside the lifecycle enum. Advance it conditionally for the same source UUID. Query remaining children by source ID, including tombstones. Remove the source record only after verifying that no owned children remain.
