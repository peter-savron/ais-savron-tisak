# SpecSourceService

SpecSourceService manages workspace-scoped [SpecSources](Spec.md#specsource), their synchronization and their OriginalSpecs. RawSpec publication access is handled by [RawSpecService](RawSpecService.md). Every operation takes the acting user’s UUID and checks ownership through the source’s workspace. Inaccessible and missing resources have the same lookup outcome.

The [spec-source CLI](../api/cli/SpecSource.md) exposes source management and synchronization by workspace and spec name.

## Core models

### SpecSyncResult

| Field | Required | Meaning |
| --- | --- | --- |
| Source | Yes | Full SpecSource whose items were reconciled. |
| OriginalSpecs | Yes | Discovered or retried items with their final discovery and processing statuses. Empty when no items changed or needed retry. |

The OriginalSpec status records the sync outcome. `UP_TO_DATE` means processing succeeded. `SELECTED` with processing status `FAILED` means processing failed; a retained active RawSpec remains available. `MISSING`, `EXCLUDED`, `ACCESS_DENIED` and `UNRESOLVED` report source outcomes. `NEW` and `CHANGED` are transient discovery values that core resolves to `UP_TO_DATE` or `SELECTED` after processing.

### SpecDiff

| Field | Required | Meaning |
| --- | --- | --- |
| Base published at | Yes | Publication timestamp of version A. |
| Target published at | Yes | Publication timestamp of version B. |
| Edits | Yes | Non-overlapping line edits in file order. Omit unchanged lines and context. Identical content yields an empty list. |
| Potentially stale | Yes | Whether the source was syncing in the same snapshot. |

### LineEdit

| Field | Required | Meaning |
| --- | --- | --- |
| Base offset | Yes | Number of lines before the edit in the base revision. |
| Target offset | Yes | Number of lines before the edit in the target revision. |
| Base lines | Yes | Removed or replaced lines. Empty for insertion. |
| Target lines | Yes | Inserted or replacement lines. Empty for deletion. |

A replacement has lines on both sides. Offsets count from zero. An insertion before the first line has a base offset of zero. An insertion after the last line has a base offset equal to the base line count. Preserve line endings where present so a change to the final newline can be represented.

## Operations

| Operation | Contract |
| --- | --- |
| Create, get, list, update or delete sources | Apply the [SpecSource management rules](Spec.md#specsource-management). Create accepts a workspace, spec name and source configuration. Keep the spec name stable across root or selection updates. Deletion prevents outstanding syncs from publishing against the deleted registration. |
| Sync source | Discover changes and process eligible items. Return the SpecSyncResult with the processed OriginalSpecs and their statuses. |
| List source items | Given a source ID, return item identities, locators, current selection and processing states, and published RawSpec UUIDs where available. Include pending or failed items even before their first publication. |
| Get, list and update RawSpec publications | These operations are provided by [RawSpecService](RawSpecService.md). |

## Removal and visibility

For LOCAL_FILES, confirmed absence and confirmed exclusion by ignore rules both soft-delete the RawSpec. Record the distinct status and physically remove its published working file. A selected file returning at the same source-relative path reuses the RawSpec UUID, including after an ignore rule is lifted. For an active publication, core hashes the currently published normalized text and new normalized output to determine whether the text changed; identical text needs only a metadata update. A soft-deleted publication stays hidden until a successful sync republishes it. A failed sync of a selected OriginalSpec leaves its previous active RawSpec available. After a root replacement, reconcile against the new scope and retire publications confirmed removed. A read error, access denial or incomplete discovery alone cannot establish removal. A soft-deleted publication is absent from normal get, list, history and diff operations, including lookup by a known UUID. Retain source/workspace provenance for authorization and later sync reconciliation.

Core decides when to publish, retire or reactivate a publication during synchronization. Storage computes each requested diff from the committed normalized texts and returns it to core. Core uses opaque revision IDs and makes the authorization and visibility decisions.

## Reads and writes during sync

Atomically claim an ACTIVE source as SYNCING before discovery. The owning sync alone may stage changes and publish them. Reject competing syncs and all other mutations of that source or its elements (including update, delete and background writes) with a source-busy outcome. Check the claim at write time so previously admitted work cannot slip through. Unrelated sources remain available.

Reads proceed against committed data. Return `potentiallyStale` with the source status from the same committed snapshot: true while SYNCING and false otherwise. Apply it to source and item reads, history, diffs, and each source represented in lists or search hits. A source-scoped empty list carries the flag on the response. This flag describes in-flight sync only; scan issues and index lag are separate.

Publish the batch and release the claim to ACTIVE atomically, including empty syncs and syncs with individual item failures. On abort preserve the prior publication and release the claim after discarding or fencing pending writes. After interruption, confirm the previous worker and its pending writes have stopped before restoring ACTIVE.

## Source deletion

1. Atomically change ACTIVE to MARKED_FOR_DELETION before cleanup. Hide the source’s publications from ordinary reads. Reject updates, syncs and late publication callbacks. Keep owner-scoped source lookup and deletion retry available. Fence and drain in-flight storage writers before touching files.
2. Delete managed RawSpec working files and the spec folder, including its local repository. Confirm the folder is absent before advancing. This is teardown of the whole source. A later source sync can republish a selected item while the source remains registered.
3. Delete RawSpec revisions and RawSpec records, then OriginalSpec records, then the SpecSource. Retain parent identifiers and ownership until dependent cleanup succeeds. Internal cleanup locates children by source ID even while hidden from ordinary reads.
4. Each phase accepts already-removed children. Persist progress on the marked source after verifying each phase, and resume at the first unfinished phase after interruption. A permissions or I/O error leaves the source marked for retry. Reserve its name through final removal and serialize retries so a stale cleanup cannot touch a newly registered source.

## Sync

1. Discover items using the current source configuration and apply the [change detection rules](Spec.md#synchronizing-originalspecs). Queue new, changed and reselected soft-deleted items and resume pending or failed observations. Discovery omits unchanged documents. Core retries stored pending or failed selected items even when discovery reports no change for them. If a previously inaccessible item becomes readable with unchanged verified content and a matching active publication, mark it DONE again. Carry forward locator or descriptive metadata changes for the same verified content and identity.
2. Fetch each eligible item and provide the [DocumentProcessor](DocumentProcessor.md) with its OriginalSpec, a stable file containing the fetched bytes, and a buffered output destination. The provider verifies that the fetched bytes match the discovery observation. A mismatch raises `VERSION_CHANGED` and leaves the item eligible for a later sync. The service releases any input or output staging resources it owns.
3. Normalize successful output as Markdown or plain text, retaining meaningful structure. Publish the text and its matching provenance together. Assign the RawSpec UUID on first publication and retain it across updates to the same source item. Mark the matching OriginalSpec observation DONE only after publication succeeds.
4. On a processing or storage failure, record FAILED for that observation and retain the previous publication. One item’s failure allows other items to finish. Confirmed absence or access denial marks the item UNAVAILABLE. Exclusion by selection rules is reported separately. Partial discovery preserves the prior state of unobserved items.
5. Before publication, verify the source configuration and observation are still current. Retries and concurrent syncs of the same observation produce one publication. A stale completion cannot overwrite a newer result or reinstate an excluded item.

The [ArcadeDB metadata contract](../integrations/arcadedb/Spec.md) and [workspace file contract](../integrations/filesystem/RawSpec.md) define publication and retrieval.
