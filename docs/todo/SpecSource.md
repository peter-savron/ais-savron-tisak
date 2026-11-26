# SpecSource

Proposed feature. MVP scope is open.

An **OriginalSpec** contains the retrieved content in its upstream format and the associated metadata. A **RawSpec** is the result of mapping and then normalizing that content into LLM-readable text, preferably Markdown or plain text. It can represent a document or an ordered sequence of chat messages.

A **SpecSource** is a workspace-scoped set of rules that identifies where to find specs. It holds the location and selection data for a **SpecType**. Each SpecType defines the data it accepts, and its [implementation](integrations/spec-source/README.md) applies that data to navigate, discover, and read items from the original source. Candidate types include GitHub, Confluence, local folders, and chat history.

## Core model

| Concept | Responsibility |
| --- | --- |
| SpecSource | Location and selection rules for a specific SpecType within a [workspace](../features/Workspace.md). |
| SpecType | Defines the source data required by its implementation to interact with the original source. |
| Source item | A retrievable document, file, conversation, or other unit defined by the integration. Use a stable upstream identifier when available and a normalized locator otherwise. Keep the current locator separately so a move can be recognized when identity survives it. |
| OriginalSpec | Upstream content with its item identity, locator, input format, available upstream revision, and original content hash. |
| RawSpec | Normalized text with its format and provenance, including the original content hash. Preserve document structure or message order when available. |
| Snapshot | An immutable synced RawSpec revision with a stable local revision ID, source metadata, mapping and normalization versions, and sync time. |
| Version diff | A reconstructable text change between consecutive synced RawSpec revisions of the same item. Links the base and target revision IDs. |

## Access

Every SpecSource operation requires the acting user's UUID. Registration also requires a workspace UUID. For operations on an existing source, core uses the workspace associated with that source. Core authorizes access against workspace ownership and verifies that the acting [user](../features/User.md) exists before registering a source.

## Operations

| Operation | Behavior |
| --- | --- |
| Check changes | Compare current source observations with the last successful sync and report added, updated, moved, removed, unchanged, access denied, or unresolved items. Include the locator and the reason for each reported change. Checking leaves the synced baseline intact. |
| Retrieve and transform | Retrieve the OriginalSpec through the SpecType implementation, map its content to text, and normalize the result into a RawSpec. Report unsupported formats and processing failures per item. |
| Sync | Sync the latest upstream version of each in-scope item. For each successful change, commit its revision metadata, any version diff, and the updated synced state together. Return an outcome for each item. |
| Get latest synced version | Return the latest committed RawSpec snapshot and its state without contacting the upstream source. Surface recorded removal and allow access to the last imported revision through history. |
| Get synced revision | Resolve a specific local revision ID and return its exact stored RawSpec without contacting the upstream source. Consumers can pin this ID across later syncs. |
| Get change history | Return an item's version diffs in revision order. Allow retrieval of the corresponding snapshots. |

## Change detection and history

- The first successful sync stores a base snapshot and records an addition. Each later successful sync makes the latest observed revision available while preserving access to earlier synced revisions.
- Use reliable upstream markers to recognize unchanged content. For a file, hash the exact retrieved bytes before mapping. For a non-file source, hash the stable serialization defined by its SpecType. Store the original content hash in the RawSpec provenance. Record the upstream revision of the content actually retrieved when available.
- SpecType implementations report access denied, confirmed missing, or unresolved when an item cannot be read. Record removal only on confirmed absence. Access errors and incomplete discovery leave the last successful snapshot intact. With locator-based identity, a confirmed disappearance and a new locator appear as separate removal and addition events.
- Compare consecutive RawSpec text revisions after mapping and normalization. Each diff must reproduce the target text exactly when applied to its base, including where text is inserted, replaced, or deleted. The implementation may use Git, custom patches, or another mechanism that provides equivalent reconstruction.
- Preserve source hash, upstream revision, and locator changes in revision metadata even when the normalized text stays identical. In that case the text diff is empty. Record changes caused by mapping or normalization as reprocessing so readers can distinguish them from upstream edits.
- Keep the complete local changelog of committed sync states. Initially, each sync fetches the latest upstream state and records at most one transition per item. A future integration that can retrieve intermediate upstream versions may import them in order and record multiple diffs during one sync.
- A failed fetch, mapping, normalization, or commit leaves the last successful state available. Retries of the same observed state must avoid duplicate revisions. Concurrent syncs must preserve revision order and avoid overwriting a newer synced state with results calculated against an older baseline.

## Integration boundaries

- SpecType implementations interpret SpecSource data to navigate the original source, discover items, and retrieve OriginalSpecs with their provenance.
- [Document mapping integrations](integrations/document-mapping/README.md) extract text from OriginalSpecs. A separate, versioned normalization step produces the stored RawSpec text. OCR provides the initial path for documents that require text extraction.
- Persistence integrations load the baseline and ordered history and commit the item state and its change record atomically. Core decides what counts as a change and when to commit it.

## Open decisions

- Which source types and input formats belong in the first release?
- Should mapping changes re-fetch upstream content or allow reprocessing of retained source material?
