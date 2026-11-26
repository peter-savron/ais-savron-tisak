# SpecSource

Proposed feature. MVP scope is open.

SpecSource brings raw specifications into a [workspace](../features/Workspace.md). It discovers available specs, reports which ones have changed since the last successful sync, and transforms retrieved content into plaintext, Markdown, or structured text. It keeps the latest synced version of each spec and an ordered history of changes so consumers such as [Feature](../features/Feature.md) can review what was imported and how it evolved.

## Core model

| Concept | Responsibility |
| --- | --- |
| Source | A workspace-scoped configuration that defines where specs come from, which items are in scope, and how they are transformed. |
| Source item | One retrievable file, URL, or object. Identity is scoped to the source. Use a stable upstream identifier when available and a normalized locator otherwise. Keep the current locator separately so a move can be recognized when identity survives it. |
| Synced revision | An immutable record of one imported state, with item identity, locator, raw content or a durable reference to it, a content fingerprint, transformation settings and version, rendered output, and sync time. |
| Change record | An ordered entry per item that links the previous and resulting revision states. Record additions, content updates, moves, and removals, with a diff when the change affects the rendered output. |

## Access

Every SpecSource operation requires the acting user's UUID. Registration also requires a workspace UUID. For operations on an existing source, core uses the workspace associated with that source. Core authorizes access against workspace ownership and verifies that the acting [user](../features/User.md) exists before registering a source.

## Operations

| Operation | Behavior |
| --- | --- |
| Check changes | Compare current source observations with the last successful sync and report added, updated, moved, removed, unchanged, or unresolved items. Include the locator and the reason for each reported change. Checking leaves the synced baseline intact. |
| Retrieve and transform | Retrieve raw content with its available upstream version metadata. Produce the requested plaintext, Markdown, or structured-text representation. Report unsupported formats and transformation failures per item. |
| Sync | Discover changes, fetch and transform affected items, then record each successful change. Advance an item's synced state and append its history entry together. Return an outcome for each item. |
| Get latest synced version | Return the latest committed state for an item without contacting its upstream source. Surface recorded removal and allow access to the last imported revision through history. |
| Get change history | Return an item's change records in revision order, with the revisions they compare and any available diffs. |

## Change detection and history

- A newly discovered item has no synced baseline. Its first successful sync records an addition and stores the initial revision.
- Use reliable upstream revision markers to avoid fetching unchanged content. Where a marker cannot establish equality, compare fingerprints of retrieved raw content. A change to transformation settings can trigger a new revision even when the raw content is identical.
- Record removal only when the source confirms it, such as through a complete inventory or an explicit deletion signal. Failed or partial discovery leaves uncertain items unresolved. With locator-based identity, a confirmed disappearance and a new locator appear as separate removal and addition events.
- Generate diffs between consecutive committed representations of the same item. Plaintext and Markdown can use line diffs. Structured text can compare sections or blocks once its schema is defined. Keep meaningful raw or locator changes in history even when the rendered diff is empty. A switch between incompatible representations records a format change and links both revisions.
- A failed fetch, transformation, or commit leaves the last successful state available for reads and retries. A retry of the same observed state must not duplicate revisions. Concurrent syncs must preserve revision order and avoid overwriting a newer synced state with results calculated against an older baseline.

## Integration boundaries

- Source integrations discover items and retrieve content together with any available identity, location, and version metadata.
- Transformation integrations convert raw content into the selected representation and identify the transformation version used.
- Persistence integrations load the baseline and ordered history and commit the item state and its change record atomically. Core decides what counts as a change and when to commit it.

## Open decisions

- Which source types and input formats belong in the first release?
- What fields and identities should structured text expose to downstream consumers?
- How long should raw and rendered revisions be retained?
- Should configuration changes reprocess the last synced raw content, fetch current upstream content, or support both?
