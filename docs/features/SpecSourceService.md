# SpecSourceService

SpecSourceService synchronizes one [SpecSource](Spec.md#specsource) at a time and serves the latest successfully published RawSpec for each item. Every operation takes the acting user’s UUID and checks ownership through the source’s workspace. Inaccessible and missing resources have the same lookup outcome.

The [spec-source CLI](../api/cli/SpecSource.md) exposes source management and synchronization by workspace and spec name.

## Core models

### SpecSyncResult

| Field | Required | Meaning |
| --- | --- | --- |
| Source ID | Yes | SpecSource whose observations were reconciled. |
| Scan complete | Yes | True for a successful sync. Conversion failures are reported independently. |
| Item outcomes | Yes | One OriginalSpecChange per detected change or retried item processed during this sync. A scan with no changes or retries returns an empty outcome list. |
| Issues | Yes | Failed or unresolved items or scopes, their locators when known, and reasons. An unresolved observation alone produces no deletion. |
| Counts by extent | Yes | Count item outcomes by extent, including zero. The changed total counts NEW, UPDATED, UPDATED_NO_RELEVANT_CHANGE, DELETED and MOVED. Count UNTRACKED, UNCHANGED, EXCLUDED and UNRESOLVED separately from that total. |
| Observed item count | When available | Number of distinct selected items discovered in this scan. |
| Publication revision | When supplied by storage | Opaque identifier for the published sync. |

### OriginalSpecChange

| Field | Required | Meaning |
| --- | --- | --- |
| OriginalSpec reference | Yes | Source ID and item identity, retained from the prior observation for deletion. |
| Locator | Yes | Current locator, or last known locator for deletion. |
| Previous locator | For MOVED | Locator before the move. |
| Change extent | Yes | One of the outcomes below. |
| RawSpec UUID | When available | Identifies the new or retained RawSpec. Omit when no publication exists. |
| Reason | For outcomes with no publication change | Explain why this sync produced no publication for the item. Include the failed stage and cause when an attempt failed. |
| Added and removed lines | When available | Counts from storage’s normalized-text diff. Omit unavailable counts; zero means measured zero. |

| Change extent | Classification |
| --- | --- |
| NEW | First publication of this source item. |
| UPDATED | The item’s published normalized text changed. |
| UPDATED_NO_RELEVANT_CHANGE | The input or its provenance changed, but the normalized text is identical. Relevance here means text equality, including whitespace. |
| DELETED | The previously published item was soft-deleted after confirmed absence or exclusion. |
| MOVED | Reserved for a source contract that can explicitly report moves. Relative-path reconciliation never infers a move. A rename to a previously unseen path yields DELETED and NEW. |
| UNTRACKED | A selected file could not be published and has no active RawSpec. Examples include an unsupported format on first sync and a failed restoration of a soft-deleted item. |
| UNCHANGED | An active RawSpec remains at its previous publication. This includes restored access to verified published input and a failed attempt to fetch, convert or publish a newer observation. |
| EXCLUDED | A known path is outside the current selection and has no active RawSpec to retire. If exclusion retires an active publication in this sync, report DELETED. |
| UNRESOLVED | The source explicitly reports an identified item whose presence or selection could not be established. Preserve its prior state and publication, if any, and report the cause as an issue. |

Classify each item once. Use UNRESOLVED for an inconclusive source observation. When the file is known to be selected and a read or processing attempt fails, use UNTRACKED or UNCHANGED according to whether an active RawSpec exists. EXCLUDED applies only to known paths; it does not require enumerating ignored subtrees.

For a failed item, also record the issue and apply the existing OriginalSpec processing status. A retained publication with an update error remains eligible for retry. Scope failures that prevent full discovery abort the sync before reconciliation.

On restoration at the same identity, compare against the last live text. Report UPDATED for different text and UPDATED_NO_RELEVANT_CHANGE for identical text, including when restoration alone changed visibility. Storage supplies text comparisons and any line counts. Core classifies the change even when detailed diff statistics are unavailable.

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
| Sync source | Discover changes and process eligible items. Return the SpecSyncResult for the committed batch, including issues for unsuccessful items. |
| List source items | Given a source ID, return item identities, locators, current selection and processing states, and published RawSpec UUIDs where available. Include pending or failed items even before their first publication. |
| Get published RawSpec | Given a RawSpec UUID, return the last successfully published metadata and text together with the current item state. Reads use the published result even if upstream is offline. An item with no successful publication has no RawSpec to get. |
| List revisions and compare | For an active RawSpec, list its published revision IDs in order. Given two revisions of that UUID, request the per-file diff from storage. Return the structured line edits in [SpecDiff](#specdiff). Metadata-only edits yield an empty text diff. |
| Roll back removal | Given the workspace and deleted RawSpec UUID, verify ownership using retained metadata and restore the last live publication under its original UUID. Return the restored revision. Repeating the same request must preserve later changes. |

## Removal and visibility

For LOCAL_FILES, confirmed absence and confirmed exclusion by ignore rules both soft-delete the RawSpec. Record the distinct reason and physically remove its published working file. A selected file returning at the same source-relative path reuses the RawSpec UUID, including after an ignore rule is lifted. On successful republication, storage compares the restored text with the last live version before removal. Identical text yields an empty diff. Until publication succeeds the tombstone remains hidden. After a root replacement, reconcile against the new scope and retire publications confirmed removed. A read error, access denial or incomplete discovery alone cannot establish removal. A soft-deleted publication is absent from normal get, list, history and diff operations, including lookup by a known UUID. Retain source/workspace provenance for authorization and recovery. Rollback restores the local publication only. The next sync evaluates upstream availability again.

Core decides when to publish, retire or restore. Storage computes each requested diff from the committed normalized texts and returns it to core. Core uses opaque revision IDs and makes the authorization and visibility decisions.

## Reads and writes during sync

Atomically claim an ACTIVE source as SYNCING before discovery. The owning sync alone may stage changes and publish them. Reject competing syncs and all other mutations of that source or its elements (including update, delete, RawSpec restore and background writes) with a source-busy outcome. Check the claim at write time so previously admitted work cannot slip through. Unrelated sources remain available.

Reads proceed against committed data. Return `potentiallyStale` with the source status from the same committed snapshot: true while SYNCING and false otherwise. Apply it to source and item reads, history, diffs, and each source represented in lists or search hits. A source-scoped empty list carries the flag on the response. This flag describes in-flight sync only; scan issues and index lag are separate.

Publish the batch and release the claim to ACTIVE atomically, including empty syncs and syncs with individual item failures. On abort preserve the prior publication and release the claim after discarding or fencing pending writes. After interruption, confirm the previous worker and its pending writes have stopped before restoring ACTIVE.

## Source deletion

1. Atomically change ACTIVE to MARKED_FOR_DELETION before cleanup. Hide the source’s publications from ordinary reads. Reject updates, syncs, restores and late publication callbacks. Keep owner-scoped source lookup and deletion retry available. Fence and drain in-flight storage writers before touching files.
2. Delete managed RawSpec working files and the spec folder, including its local repository. Confirm the folder is absent before advancing. This is teardown of the whole source. Individual-file rollback applies while the source and repository exist.
3. Delete RawSpec revisions and RawSpec records, then OriginalSpec records, then the SpecSource. Retain parent identifiers and ownership until dependent cleanup succeeds. Internal cleanup locates children by source ID even while hidden from ordinary reads.
4. Each phase accepts already-removed children. Persist progress on the marked source after verifying each phase, and resume at the first unfinished phase after interruption. A permissions or I/O error leaves the source marked for retry. Reserve its name through final removal and serialize retries so a stale cleanup cannot touch a newly registered source.

## Sync

1. Discover items using the current source configuration and apply the [change detection rules](Spec.md#synchronizing-originalspecs). Queue new, changed and reselected soft-deleted items and resume pending or failed observations. Discovery omits unchanged documents. Core retries stored pending or failed selected items even when discovery reports no change for them. The scan count includes unchanged selected files. If a previously inaccessible item becomes readable with unchanged verified content and a matching active publication, restore its DONE state. Carry forward locator or descriptive metadata changes for the same verified content and identity.
2. Fetch each eligible item and provide the [DocumentProcessor](DocumentProcessor.md) with its OriginalSpec, a stable file containing the fetched bytes, and a buffered output destination. The provider verifies that the fetched bytes match the discovery observation. A mismatch raises `VERSION_CHANGED` and leaves the item eligible for a later sync. The service releases any input or output staging resources it owns.
3. Normalize successful output as Markdown or plain text, retaining meaningful structure. Publish the text and its matching provenance together. Assign the RawSpec UUID on first publication and retain it across updates to the same source item. Mark the matching OriginalSpec observation DONE only after publication succeeds.
4. On a processing or storage failure, record FAILED for that observation and retain the previous publication. One item’s failure allows other items to finish. Confirmed absence or access denial marks the item UNAVAILABLE. Exclusion by selection rules is reported separately. Partial discovery preserves the prior state of unobserved items.
5. Before publication, verify the source configuration and observation are still current. Retries and concurrent syncs of the same observation produce one publication. A stale completion cannot overwrite a newer result or reinstate an excluded item.

The [ArcadeDB metadata contract](../integrations/arcadedb/Spec.md) and [workspace file contract](../integrations/filesystem/RawSpec.md) define publication and retrieval.
