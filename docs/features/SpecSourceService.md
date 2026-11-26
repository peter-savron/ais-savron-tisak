# SpecSourceService

SpecSourceService manages workspace-scoped [SpecSources](Spec.md#specsource), their synchronization and their Specs. RawSpec publication access is handled by [RawSpecService](RawSpecService.md). Every operation takes the acting user’s UUID and checks ownership through the source’s workspace. Inaccessible and missing resources have the same lookup outcome.

The [spec-source CLI](../api/cli/SpecSource.md) exposes source management and synchronization by workspace and spec name.

## Core models

### SpecSyncResult

| Field | Required | Meaning |
| --- | --- | --- |
| Source | Yes | Full SpecSource whose items were reconciled. |
| Specs | Yes | Discovered or retried items with their final discovery and processing statuses. Empty when no items changed or needed retry. |

The Spec status records the sync outcome. `UP_TO_DATE` means processing succeeded. Empty reads and read or processing exceptions set status `UNAVAILABLE` and processing status `FAILED`; a retained active RawSpec remains available. An upstream deletion record sets status `DELETED` and processing status `UNAVAILABLE`. `EXCLUDED`, `ACCESS_DENIED` and `UNRESOLVED` report source outcomes. Successfully processed `NEW`, `UPDATED`, `MOVED` and `REINTEGRATED` Specs become `UP_TO_DATE`.

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
| Sync source | Discover changes and process eligible items. Return the SpecSyncResult with the processed Specs and their statuses. |
| List source items | Given a source ID, return item identities, locators, current selection and processing states, and published Spec UUIDs where available. Include pending or failed items even before their first publication. |
| Get, list and update RawSpec publications | These operations are provided by [RawSpecService](RawSpecService.md). |

## Removal and visibility

For LOCAL_FILES, confirmed absence and confirmed exclusion by ignore rules both soft-delete the publication. Record the distinct status and physically remove its published working file. A selected file returning at the same source-relative path reuses the Spec UUID, including after an ignore rule is lifted. For an active publication, core compares the currently published normalized text with the new normalized output to determine whether the text changed; identical text needs only a metadata update. A soft-deleted publication stays hidden until a successful sync republishes it. A failed sync of a selected Spec leaves its previous active publication available. After a root replacement, reconcile against the new scope and retire publications confirmed removed. A read error, access denial or incomplete discovery alone cannot establish removal. A soft-deleted publication is absent from normal get, list, history and diff operations, including lookup by a known UUID. Retain source/workspace provenance for authorization and later sync reconciliation.

Core decides when to publish, retire or reactivate a publication during synchronization. Storage computes each requested diff from the committed normalized texts and returns it to core. Core uses opaque revision IDs and makes the authorization and visibility decisions.

## Reads and writes during sync

Atomically claim an ACTIVE source as SYNCING before discovery. The owning sync alone may stage changes and publish them. Reject competing syncs and all other mutations of that source or its elements (including update, delete and background writes) with a source-busy outcome. Check the claim at write time so previously admitted work cannot slip through. Unrelated sources remain available.

Reads proceed against committed data. Return `potentiallyStale` with the source status from the same committed snapshot: true while SYNCING and false otherwise. Apply it to source and item reads, history, diffs, and each source represented in lists or search hits. A source-scoped empty list carries the flag on the response. This flag describes in-flight sync only; scan issues and index lag are separate.

Publish the batch and release the claim to ACTIVE atomically, including empty syncs and syncs with individual item failures. On abort preserve the prior publication and release the claim after discarding or fencing pending writes. After interruption, confirm the previous worker and its pending writes have stopped before restoring ACTIVE.

## Source deletion

1. Atomically change ACTIVE to MARKED_FOR_DELETION before cleanup. Hide the source’s publications from ordinary reads. Reject updates, syncs and late publication callbacks. Keep owner-scoped source lookup and deletion retry available. Fence and drain in-flight storage writers before touching files.
2. Delete managed RawSpec working files and the spec folder, including its local repository. Confirm the folder is absent before advancing. This is teardown of the whole source. A later source sync can republish a selected item while the source remains registered.
3. Delete publication and observation revisions and their Spec records before removing the SpecSource. Retain parent identifiers and ownership until dependent cleanup succeeds. Internal cleanup locates children by source ID even while hidden from ordinary reads.
4. Each phase accepts already-removed children. Persist progress on the marked source after verifying each phase, and resume at the first unfinished phase after interruption. A permissions or I/O error leaves the source marked for retry. Reserve its name through final removal and serialize retries so a stale cleanup cannot touch a newly registered source.

## Sync

### Discovery and reconciliation resource use

Discovery returns the complete selected `RemoteSpec` list for a source. Reconciliation loads that source's compact `SpecIdentifiers` once and builds an in-memory index by ID, external ID and hashes. For each discovered item that already exists, reconciliation also reads its full `Spec` record from the repository, including when it later determines that the item is unchanged. Content is read and converted one item at a time through files rather than retained for the whole source.

The bulk identifier lookup uses memory proportional to the number of stored items for the source, with several index maps referencing the identifiers. The discovered list also remains in memory during reconciliation. A very large source can therefore exhaust heap even though the document contents are processed sequentially. Fetching each existing record from storage during discovery would reduce the in-memory index, but would add up to one storage round trip per discovered item, increasing database work, I/O and sync duration. Prefer the single compact bulk lookup for normal sources.

If supported source sizes grow beyond a safe heap budget, bound memory by paging or streaming discovery and reconciling in batches, with a batched identifier lookup for each page. Preserve the complete-scan rule: do not publish absence or removal decisions from a partial scan. Avoid bulk-loading full `Spec` records or document contents when only identity and change-detection fields are needed. Measure representative source sizes before introducing a fixed limit; heap use depends on the number and size of identifiers, maps and discovery metadata, not only the number of specs.

### Sync delegation

The source service owns the public operation and source authorization. Sync processing is delegated to a CDI bean (`SpecSynchronizer`), while the source service currently owns the `ACTIVE` to `SYNCING` claim, completion result and release-on-failure behavior.

If sync orchestration needs an independent test seam or another implementation, define a core interface for the sync action and inject its CDI implementation into the source service. Keep the claim, processing and completion/abort lifecycle under one sync-action contract so callers cannot accidentally bypass source fencing. The interface is an architectural seam; it does not improve CPU, memory or power use by itself. With one implementation and no need to substitute it, the current CDI bean delegation is sufficient.

1. Discover items using the current source configuration and apply the [change detection rules](Spec.md#synchronizing-specs). Queue new, changed and reselected soft-deleted items and resume pending or failed observations. Discovery omits unchanged documents. Core retries stored pending or failed selected items even when discovery reports no change for them. If a previously inaccessible item becomes readable with unchanged verified content and a matching active publication, mark it DONE again. Carry forward locator or descriptive metadata changes for the same verified content and identity.
2. Fetch each eligible item and pass its stable input `Path` and a caller-owned output destination to the [DocumentProcessor](DocumentProcessor.md). The provider verifies that fetched bytes match the discovery observation. A mismatch raises `VERSION_CHANGED` and leaves the item eligible for a later sync. The caller owns and releases temporary input and output resources.
3. Normalize successful output as Markdown or plain text, retaining meaningful structure. Publish the text and its matching provenance together. Keep the UUID assigned when the item was first recorded. Mark the matching Spec observation DONE only after publication succeeds.
4. On an empty read or a read, processing or storage exception, set status `UNAVAILABLE`, record processing status `FAILED` and retain the previous publication. One item’s failure allows other items to finish. A `DELETED` discovery record retires the matching publication only when its Spec is `UP_TO_DATE`; deletion is skipped if the Spec was modified in the same sync. Partial discovery preserves the prior state of unobserved items.
5. Before publication, verify the source configuration and observation are still current. Retries and concurrent syncs of the same observation produce one publication. A stale completion cannot overwrite a newer result or reinstate an excluded item.

The [ArcadeDB metadata contract](../integrations/arcadedb/Spec.md) and [workspace file contract](../integrations/filesystem/RawSpec.md) define publication and retrieval.
