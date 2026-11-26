# Spec

A workspace uses a SpecSource to select upstream items. One Spec represents each upstream item and its optional normalized publication. Each publication can have multiple saved revisions.

## Core models

### SpecSource

| Field | Required | Meaning |
| --- | --- | --- |
| Source ID | Yes | Identifies the source referenced by Specs. |
| Spec name | Yes | Stable directory name assigned at creation, unique within the workspace. See [SpecSourceService](SpecSourceService.md). |
| Workspace UUID | Yes | Identifies the [workspace](Workspace.md) that owns the source. |
| Status | Yes | Lifecycle enum. Create as `ACTIVE`. An admitted sync transitions to `SYNCING` and returns to `ACTIVE` at completion or abort. Reads during sync carry the [freshness flag](SpecSourceService.md#reads-and-writes-during-sync). Deletion transitions from `ACTIVE` to `MARKED_FOR_DELETION`, which permits only deletion progress as a mutation. |
| SpecType | Yes | Defines the accepted location and selection data. |
| Source root | Yes | String identifying the upstream location. The selected SpecType defines its accepted syntax, validates the supplied string and interprets its value. |
| Subfolder | No | Selector within the root. Its syntax and interpretation belong to the SpecType. Preserve the supplied expression for type-specific validation, for example `/spec` within a monorepo or a page subtree within a Confluence space. |
| Selection rules | Yes | Determine which items to include within the configured scope. |

### RemoteSpec

A provider reports upstream facts and its discovery outcome in a RemoteSpec. Its discovery status is `UNCHANGED`, `MOVED`, `MODIFIED` or `DELETED`; application `Spec.Status` values are assigned by the synchronizer.

| Field | Required | Meaning |
| --- | --- | --- |
| External ID | Yes | Provider-scoped key used to retrieve the upstream item. |
| External hash | When available | Provider-reported hash used by core to detect upstream content changes and recognize moved items. |
| File name | When available | Current upstream filename. |
| Updated at | When available | Upstream modification time. |
| Moved from | When moved | Previous source-scoped key. |
| Moved to | When moved | Current source-scoped key. |
| Discovery status | Yes | `UNCHANGED` leaves the Spec alone, `MOVED` processes it at its new key, `MODIFIED` enters the processing pipeline, and `DELETED` enters the deletion pipeline. |

### Spec

A Spec records the latest upstream observation. Its published version retains the provenance of the last successful conversion. The upstream bytes and the stored normalized text are retrieved separately.

| Field | Required | Meaning |
| --- | --- | --- |
| Spec UUID | Yes once recorded | Stable across observations, publication, soft deletion and reactivation. |
| Source ID | Yes | ID of the SpecSource used to read the item. |
| Item identity | Yes | The normalized path relative to the effective spec root is the sole identity within a source. The source supplies the lookup scope. A content hash identifies an observation of the item. |
| Current locator | Yes | Relative path used to retrieve this observation. |
| External hash | When available | Hash reported during discovery and compared against the previous sync snapshot. |
| Input format | Yes | Upstream format of the item, which may be binary. The provider supplies this provenance; document processing does not mutate the Spec. |
| `status` | Yes | Synchronization classification. `UP_TO_DATE` means processing succeeded. `NEW`, `UPDATED`, `MOVED` and `REINTEGRATED` describe changes awaiting processing; `DELETED` marks an upstream deletion record; `UNAVAILABLE` marks an empty read or read/processing exception. `EXCLUDED`, `ACCESS_DENIED` and `UNRESOLVED` describe selection or access outcomes. |
| Original content hash | After a successful read | Hash of the exact retrieved bytes for a file or of the SpecType’s stable serialization for other content. |
| Upstream revision | When available | Upstream revision of the content actually retrieved. |
| Brief metadata | Yes | Short plain-text description for an LLM giving the item’s title or name and its origin, such as “Release checklist from the Platform team wiki.” When the title is unavailable, use the item locator and source type. |
| `processingStatus` | Yes | State of the observed item. See the transitions below. |
| Published version | After first publication | Snapshot of the provenance and output format. Retained after failure or soft deletion. |
| Deleted | Yes | Whether the publication has been retired from ordinary reads. |
| `ignored` | Yes | Defaults to `false`. When `true`, limits application access to this Spec's publication and content. Synchronization still tracks and processes it, preserving this flag. |
| Potentially stale | Yes | Whether the source was syncing when the repository read the snapshot. |

A new, changed, moved or reintegrated reference is `PENDING`. The feature that continues processing marks that version `DONE` on success or `FAILED` on an exception. An empty read or read/processing exception sets Spec `status` to `UNAVAILABLE` and processing status to `FAILED`. An upstream deletion record sets `status` to `DELETED` and processing status to `UNAVAILABLE`. Rediscovery after unavailability or an explicit processing retry returns the item to `PENDING`. A completion for an older observation cannot overwrite the state of a newer one. Successful synchronization sets `status` to `UP_TO_DATE` and processing status to `DONE`.

`status` describes how discovery resolved the upstream item and whether it is current. `processingStatus` describes whether work for that item is pending, complete, failed or unavailable. Keep these as separate dimensions: one `Status` enum would need separate pending and failed values for every change kind. To find every item that did not complete successfully, filter `processingStatus != DONE`; use `status` to identify the reason or change type.

### PublishedVersion

| Field | Required | Meaning |
| --- | --- | --- |
| Locator | Yes | Upstream location used for this publication. |
| Input format | Yes | Format of the upstream content. |
| Content hash | When available | Hash of the bytes used for publication. |
| Upstream revision | When available | Upstream version used for publication. |
| Metadata | Yes | Description associated with the published input. |
| Output format | Yes | MIME type of the separately stored normalized text. |

## SpecSource management

Every operation requires the acting user’s UUID. Core verifies workspace ownership. A source in another user’s workspace has the same lookup outcome as a missing source.

| Operation | Behavior |
| --- | --- |
| Create | Verify that the [user](User.md) exists and owns the supplied workspace. Validate the source configuration for its SpecType and assign a stable source ID. |
| Get | Return the source configuration by source ID. |
| List | Return the sources of the supplied workspace, including an empty list when applicable. |
| Update | Validate changes to the root, subfolder or selection rules. Preserve omitted fields and the source ID. Changing the root is supported but discouraged because an existing relative locator may identify a different file. Reconcile observations against the new scope before reusing processing results. |
| Delete | Mark the source and perform the [ordered deletion](SpecSourceService.md#source-deletion) before removing its registration. Source management never modifies the upstream files. |

## Synchronizing Specs

Sync discovery either returns the complete current selection or fails. It uses only the SpecSource configuration; it does not depend on previously stored Specs. Core resolves returned items against stored Specs by external ID and external hash. Only Specs resolved as `NEW`, `UPDATED`, `MOVED` or `REINTEGRATED` are queued for processing; unchanged Specs are left out. A new path is an addition. Different bytes at the same path are an update. A rename can reuse the existing Spec when its external hash matches.

The `ignored` flag does not affect discovery, reconciliation or processing. Synchronization preserves it for existing Specs. Ignored Specs remain tracked, but RawSpec publication listings and workspace-authorized publication, content, revision and diff reads omit them.

When retrieving items from an upstream source, the provider should use as `externalId` the document or object field most likely to preserve identity across mutations. Prefer an immutable source-assigned identifier over mutable fields such as a path or title. If no suitable stable field exists, define a move-resolution mechanism that can relate the previous and current external IDs. When the provider cannot resolve a move directly, the synchronizer may use additional move-detection algorithms over available hashes or metadata. Such a match must not merge distinct items when identity is ambiguous.

Report confirmed disappearance separately from exclusion by changed selection rules. If discovery cannot cover the full selection, fail the sync before applying observations. Report individual read failures with the affected locator and reason. Record only verified observations as the new comparison baseline. The [DocumentProcessor](DocumentProcessor.md) transforms a file supplied for an Spec. Normalization follows conversion.

## SpecProvider

A [SpecSource](#specsource) stores one workspace’s configuration. A `SpecProvider` is the outgoing port through which core validates that configuration, discovers upstream items and reads their content. Each provider serves one SpecType. Register its CDI bean with an explicit `@Named` value matching the stable, case-sensitive type identifier. Core uses that name to resolve the provider. Core checks workspace ownership before discovery and reads.

| Operation | Provider contract |
| --- | --- |
| `describeConfiguration()` | Return a nonempty Markdown guide with required and optional settings, root and subfolder syntax, selection rules and an example. This operation takes no arguments and works before source creation, without an upstream connection. |
| `validate(source)` | Check the proposed source configuration against the type’s requirements on creation or update. Reject invalid settings. |
| `discover(source)` | Apply the current selection rules within the root and optional subfolder. Return all selected RemoteSpecs containing upstream hashes and other upstream facts, without application statuses. Return only after the complete selection has been scanned; raise DISCOVERY_FAILED if any issue prevents a complete result. |
| `read(source, observed)` | Reassess the same source and item identity at read time. A successful `Read` supplies a stable file containing the observed version. Throw a reason-coded read exception if the content cannot be retrieved or its version has changed. Core passes the file to the [DocumentProcessor](DocumentProcessor.md) and closes the read handle after conversion. |

The synchronizer processes `MOVED` RemoteSpecs first, then `MODIFIED`, then `DELETED`. A move resolves the existing Spec using `movedFrom` (or the external hash when no previous key is supplied), changes its external ID to `movedTo`, and runs the processing pipeline at the new location. A modified item resolves by external ID first, then by external hash. It is `NEW` when neither matches, `UPDATED` when the matched item's hash is absent or changed, or `REINTEGRATED` when the matched Spec was deleted. If reading a modified item returns empty or throws, its status becomes `UNAVAILABLE`. A deleted record with no matching Spec is ignored. Deletion applies only to an `UP_TO_DATE` Spec; if a moved or modified item resolved to that Spec during the same scan, the deletion is skipped. Discovery issues that prevent a complete result are thrown by the provider before reconciliation.

For example, the `LOCAL_FILES` configuration with root `file:///work/repo` and subfolder `spec` reads selection rules from `/work/repo/spec/.specignore`. A line containing `/drafts/` excludes the top-level drafts directory. A regular file at `api/order.md` remains selected under this rule. If its bytes change between discovery and retrieval, the read fails with `VERSION_CHANGED`; a later complete discovery and read can process the new version.

The [LOCAL_FILES integration](../integrations/filesystem/LocalFiles.md) defines filesystem discovery and reads. The [post-MVP sync and history proposal](../todo/SpecSource.md) describes how core can use these observations and the resulting publication.
