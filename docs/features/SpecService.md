# SpecService

SpecService synchronizes one [SpecSource](Spec.md#specsource) at a time and serves the latest successfully published RawSpec for each item. Every operation takes the acting user’s UUID and checks ownership through the source’s workspace. Inaccessible and missing resources have the same lookup outcome.

The [Spec CLI](../api/cli/Spec.md) exposes source management and synchronization by workspace and spec name.

## Core models

### SpecSyncResult

| Field | Required | Meaning |
| --- | --- | --- |
| Sync ID | Yes | Identifies this sync of one source, including across recovery of the same invocation. |
| Source ID | Yes | SpecSource whose observations were reconciled. |
| Scan complete | Yes | Whether discovery covered the entire selection. Conversion failures are reported independently. |
| Changes | Yes | One OriginalSpecChange per item whose new state was published by this sync. Empty for a no-change sync. |
| Issues | Yes | Failed or unresolved items or scopes, their locators when known, and reasons. An unresolved observation alone produces no deletion. |
| Counts by extent | Yes | Count entries in Changes for each extent, including zero. Their sum is the total changed. |
| Observed item count | When available | Number of distinct selected items discovered in this scan. On incomplete discovery it counts only the observed portion. |
| Publication revision | When supplied by storage | Opaque identifier for the published sync. |

### OriginalSpecChange

| Field | Required | Meaning |
| --- | --- | --- |
| OriginalSpec reference | Yes | Source ID and item identity, retained from the prior observation for deletion. |
| Locator | Yes | Current locator, or last known locator for deletion. |
| Previous locator | For MOVED | Locator before the move. |
| Change extent | Yes | One of the five values below. |
| RawSpec UUID | When available | Links the change to its publication. |
| Added and removed lines | When available | Counts from storage’s normalized-text diff. Omit unavailable counts; zero means measured zero. |

| Change extent | Classification |
| --- | --- |
| NEW | First publication of this source item. |
| UPDATED | The item’s published normalized text changed. |
| UPDATED_NO_RELEVANT_CHANGE | The input or its provenance changed, but the normalized text is identical. Relevance here means text equality, including whitespace. |
| DELETED | The previously published item was soft-deleted after confirmed absence or exclusion. |
| MOVED | The same stable source identity has a different locator. This takes precedence over a simultaneous text edit; any diff statistics describe that edit. Path-identified sources represent a rename as DELETED plus NEW. |

On restoration at the same identity, compare against the last live text. Report UPDATED for different text and UPDATED_NO_RELEVANT_CHANGE for identical text, including when restoration alone changed visibility. An unchanged active item contributes only to the observed count. Storage supplies text comparisons and any line counts. Core classifies the change even when detailed diff statistics are unavailable.

## Operations

| Operation | Contract |
| --- | --- |
| Create, get, list, update or delete sources | Apply the [SpecSource management rules](Spec.md#specsource-management). Create accepts a workspace, spec name and source configuration. Keep the spec name stable across root or selection updates. Deletion prevents outstanding syncs from publishing against the deleted registration. |
| Sync source | Discover changes and process eligible items. Return the SpecSyncResult for the committed batch, including issues for unsuccessful items. |
| List specs | Given a source ID, return item identities, locators, current selection and processing states, and published RawSpec UUIDs where available. Include pending or failed items even before their first publication. |
| Get spec | Given a RawSpec UUID, return the last successfully published metadata and text together with the current item state. Reads use the published result even if upstream is offline. An item with no successful publication has no RawSpec to get. |
| List revisions and compare | For an active RawSpec, list its published revision IDs in order. Given two revisions of that UUID, request the per-file diff from storage. Return the base and target IDs and the storage-produced text changes. Metadata-only edits yield an empty text diff. |
| Roll back removal | Given the workspace and deleted RawSpec UUID, verify ownership using retained metadata and restore the last live publication under its original UUID. Return the restored revision. Repeating the same request must preserve later changes. |

## Removal and visibility

For LOCAL_FILES, confirmed absence and confirmed exclusion by ignore rules both soft-delete the RawSpec. Record the distinct reason and physically remove its published working file. A selected file returning at the same source-relative path reuses the RawSpec UUID, including after an ignore rule is lifted. On successful republication, storage compares the restored text with the last live version before removal. Identical text yields an empty diff. Until publication succeeds the tombstone remains hidden. After a root replacement, reconcile against the new scope and retire publications confirmed removed. A read error, access denial or incomplete discovery alone cannot establish removal. A soft-deleted publication is absent from normal get, list, history and diff operations, including lookup by a known UUID. Retain source/workspace provenance for authorization and recovery. Rollback restores the local publication only. The next sync evaluates upstream availability again.

Core decides when to publish, retire or restore. Storage computes each requested diff from the committed normalized texts and returns it to core. Core uses opaque revision IDs and makes the authorization and visibility decisions.

## Source deletion

1. Atomically change ACTIVE to MARKED_FOR_DELETION before cleanup. Hide the source’s publications from ordinary reads. Reject updates, syncs, restores and late publication callbacks. Keep owner-scoped source lookup and deletion retry available. Fence and drain in-flight storage writers before touching files.
2. Delete managed RawSpec working files and the spec folder, including its local repository. Confirm the folder is absent before advancing. This is teardown of the whole source. Individual-file rollback applies while the source and repository exist.
3. Delete RawSpec revisions and RawSpec records, then OriginalSpec records, then the SpecSource. Retain parent identifiers and ownership until dependent cleanup succeeds. Internal cleanup locates children by source ID even while hidden from ordinary reads.
4. Each phase accepts already-removed children. Persist progress on the marked source after verifying each phase, and resume at the first unfinished phase after interruption. A permissions or I/O error leaves the source marked for retry. Reserve its name through final removal and serialize retries so a stale cleanup cannot touch a newly registered source.

## Sync

1. Discover items using the current source configuration and apply the [change detection rules](Spec.md#synchronizing-originalspecs). Queue new or changed observations and resume pending or failed observations. Skip successfully published unchanged content. Carry forward locator or descriptive metadata changes for the same verified content and identity.
2. Fetch each eligible item and provide the [DocumentProcessor](DocumentProcessor.md) with its OriginalSpec, a stable file containing the fetched bytes, and a buffered output destination. Verify that the fetched bytes match the observation. If they differ, record a fresh observation and process that version. The service releases any input or output staging resources it owns.
3. Normalize successful output as Markdown or plain text, retaining meaningful structure. Publish the text and its matching provenance together. Assign the RawSpec UUID on first publication and retain it across updates to the same source item. Mark the matching OriginalSpec observation DONE only after publication succeeds.
4. On a processing or storage failure, record FAILED for that observation and retain the previous publication. One item’s failure allows other items to finish. Confirmed absence or access denial marks the item UNAVAILABLE. Exclusion by selection rules is reported separately. Partial discovery preserves the prior state of unobserved items.
5. Before publication, verify the source configuration and observation are still current. Retries and concurrent syncs of the same observation produce one publication. A stale completion cannot overwrite a newer result or reinstate an excluded item.

The [ArcadeDB metadata contract](../integrations/arcadedb/Spec.md) and [workspace file contract](../integrations/filesystem/RawSpec.md) define publication and retrieval.
