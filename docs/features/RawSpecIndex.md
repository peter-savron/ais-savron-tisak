# RawSpecIndex

Name and path search finds an active published [RawSpec](Spec.md#rawspec) by its source name, basename or meaningful parts of its source-relative path. Semantic search finds passages by meaning and returns their RawSpec UUID, publication revision and passage location. Resolve the acting user and workspace before retrieval. Enforce current access, publication visibility and revision for both search paths, including when an asynchronous index still contains an older result.

Every workspace starts with embeddings disabled. BM25 keyword search covers source names, relative paths and normalized published RawSpec content by default. Index the document name and path once per RawSpec. Index content for BM25 by chunk, including in lexical-only workspaces. When embeddings are enabled, encode content at chunk granularity. Content indexing follows publication, replacement, soft deletion and restoration. An explicit semantic request in a workspace without an active generation reports that embeddings are disabled. Hybrid search in that workspace returns lexical results only. Exact relative-path lookup uses the source’s existing identity rules. Keyword queries support literal terms and token prefixes. For example, `api/pay*` matches adjacent path segments beginning with `api/pay`; verify directory boundaries and prefix order against the stored relative path. A semantic query uses the authorized workspace’s active generation’s query encoder and searches only that generation. Hybrid search can merge the two ranked lists by reciprocal rank and group passages by RawSpec UUID.

## Core models

### RawSpecIndex

| Field | Meaning |
| --- | --- |
| Workspace UUID | Identifies the owning workspace and its single RawSpecIndex. |
| Active configuration revision | References the changelog entry that supplies this index’s chunker and optional embedder. |

### RawSpec chunk

| Field | Meaning |
| --- | --- |
| Publication | Workspace, RawSpec UUID and exact publication revision that supplied the text. |
| Recipe and ordinal | Chunking recipe version and zero-based position within its ordered output. |
| Span | Start (inclusive) and end (exclusive) in Unicode code points of the normalized published text. |
| Text | Exact passage covered by the span, preserving whitespace. |
| Content hash | SHA-256 over the UTF-8 bytes of the chunk text. Equal text at different positions shares a hash and retains distinct occurrences. |

### RawSpecIndex configuration revision

Each workspace’s RawSpecIndex changelog is the authoritative configuration. Each configuration entry contains a complete immutable snapshot.

| Field | Meaning |
| --- | --- |
| Workspace UUID | Identifies the index being configured. |
| Revision ID | Identifies this snapshot and is pinned by every build attempt. |
| Predecessor | Expected active configuration revision when applying this change. Unset for initialization. |
| Chunker | Required implementation ID, version and settings, including boundaries and overlap. Together these identify the chunking recipe. |
| Embedder | Optional implementation ID, version and immutable embedding profile. An absent embedder selects lexical-only indexing. |

### Embedding profile

| Field | Meaning |
| --- | --- |
| Workspace UUID | Identifies the sole workspace owning this profile and its configuration. |
| Profile ID | Immutable identity for a compatible indexing and query configuration. |
| Embedding type | DENSE or LEARNED_SPARSE. Persist it with the model revision and vector contract. A lexical-only workspace has no embedding type. |
| Encoder | Provider, immutable model or artifact revision, and document and query encoding instructions. |
| Vector contract | Output dimensions, encoding, normalization and similarity metric. |
| Encoder input preparation | Versioned preparation applied by the encoder to the chunk or query text. |
| Credential reference | Reference to an application-managed secret where required. Rotating a credential for the same encoder preserves the profile. |

### Index configuration migration

| Field | Meaning |
| --- | --- |
| Workspace UUID | Scopes the job, progress, generations and audit events to one workspace. |
| Migration ID | Identifies the requested change across all attempts. |
| Generations | Expected active and candidate index generations with their configuration revision IDs. |
| State | Requested, building, validating, activated, failed, cancelled or rolled back. |
| Progress | Snapshot boundary, per-source sync checkpoints, successful and failed passage counts, and validation results. |
| Audit | Requesting workspace owner, timestamps and append-only transition events with reasons. |

### Migration run

| Field | Meaning |
| --- | --- |
| Run ID | Unique attempt linked to its migration ID and, for a rerun, the preceding attempt. |
| Workspace sequence | Increasing number assigned on acceptance, used to identify the latest run even when timestamps coincide. |
| Timing and phase | Request time, start and finish times when available, and current or final phase. |
| Result | Pending until terminal, then SUCCEEDED, FAILED, INTERRUPTED or CANCELLED. Record the resulting active generation, checkpoint, processed and failed counts, and a failure reason and retryability when applicable. |

## Indexing interfaces

| Interface | Input | Output |
| --- | --- | --- |
| Chunker | A RawSpec publication, its normalized text and a versioned chunking recipe | An ordered sequence of RawSpec chunks. The same text and recipe yield the same spans, text and hashes. Empty text yields zero chunks and leaves the document name and path searchable. |
| Embedder | Workspace embedding profile, document or query role, and identified text inputs (chunk text for documents) | One outcome per input ID: a vector conforming to the profile or a failure with reason and retryability. If the entire call fails, every unacknowledged input has a failed outcome. |

Persist each chunk node before requesting its vector. Resolve both interfaces from the configuration revision for the generation being built or queried. A lexical-only workspace still uses its configured chunker.

## Embedding failures and repair

A failed embedding leaves the RawSpec and its chunk nodes available to lexical search. Mark the affected chunk’s vector entry NEEDS_REVIEW in its workspace generation, retain the reason, and mark that generation STALE. Expose the workspace’s active vector index as STALE whenever its active generation is affected. A candidate failure blocks cutover and leaves a healthy active generation serving. Successful siblings remain searchable; semantic and hybrid results report degraded vector coverage while the active index is stale.

Retry affected entries against their current publication, chunk hash and profile. Discard a stale completion if the publication was deleted, superseded or rechunked. A validated replacement vector becomes READY. Clear STALE only after verifying current coverage and that no eligible entry still needs review. An intentionally disabled vector index has no failed entries.

## Changelog operations

**Get latest run** resolves the workspace owner and returns the highest-sequence attempt, including its IDs, phase, timestamps, progress and recorded result. Return an empty result when the workspace has never run an index configuration change. A running attempt reports its current progress.

**Rerun change** accepts the workspace and migration ID. After a retryable failure or interruption, start a linked attempt with the same configuration revision and candidate generation. Resume from the last durable checkpoint and replay failed or unacknowledged work, including after a provider connection drops. Reconcile the committed generation before retrying a cutover with an unknown outcome. Return the existing run for an outstanding or already successful change. If the expected predecessor changed or either implementation or its settings must change, require a new migration. Preserve each attempt’s original result.

## Initialization and changes

Workspace creation creates one RawSpecIndex object. Resolve the default chunker from the application’s RawSpecIndex configuration and validate its settings. Record the resolved implementation, version and settings in the first active changelog entry, with the embedder unset. If the default cannot be resolved or validated, fail workspace creation. Later changes to the application default apply to new workspaces; existing indexes retain their recorded configuration. BM25 is available from that revision. On restart resolve the workspace’s active changelog entry. Latest-run status may describe a newer pending or failed change while reads still use the previous configuration.

The workspace owner changes either component by submitting a new configuration entry. Changing the chunker rebuilds content BM25 and, when enabled, vectors from the new chunks. Changing only the embedder can reuse existing chunks and lexical postings. Disabling embeddings retains BM25. Activate the new configuration and its index references together after validating affected indexes. Failed or cancelled runs leave the active configuration intact. Rollback records a new changelog transition referencing the restored configuration.

## Side-by-side reindex and cutover

1. Validate the chunker settings and candidate storage capacity. If an embedder is configured, also validate provider access, model revision and vector shape. Create an isolated generation within the authorized workspace. Keep the active generation serving queries.
2. Backfill from committed RawSpec text. Key work by generation, RawSpec UUID, publication revision and chunk ordinal. Persist checkpoints so retries replace the same entries. Replay subsequent publications, restorations, exclusions and deletions. Fence source deletion and discard stale work.
3. At cutover establish a publication barrier across that workspace’s sources, catch up to its checkpoints and verify coverage of all current eligible revisions, expected chunk counts and lexical coverage. For embedding-enabled generations also verify dimensions and finite vector values. Run representative queries against agreed relevance criteria. Unresolved failures block activation.
4. Atomically switch the active generation if the expected predecessor still matches. Pin query encoding and lookup to one generation per request. Resume publication updates after the switch. A crash before the switch leaves the old generation active; after the switch the stored pointer determines the outcome.
5. Retain the old generation through a configured rollback window. Before rollback, catch it up and validate it against current publications under the same barrier. Switch the pointer and append an audit event. Retire a generation only after its rollback window and in-flight queries have ended.

The [ArcadeDB index contract](../integrations/arcadedb/RawSpecIndex.md) defines storage, lexical retrieval and the operational migration journal. The initial encoder, chunk recipe, rollback window, and relevance acceptance thresholds remain deployment decisions.
