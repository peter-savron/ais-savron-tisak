# Spec

A workspace uses a SpecSource to select upstream items. An OriginalSpec points to an upstream item and records its metadata. Mapping and normalizing that OriginalSpec yields a RawSpec.

## Core models

### SpecSource

| Field | Required | Meaning |
| --- | --- | --- |
| Source ID | Yes | Identifies the source referenced by OriginalSpecs and RawSpecs. |
| Spec name | Yes | Stable directory name assigned at creation, unique within the workspace. See [SpecSourceService](SpecSourceService.md). |
| Workspace UUID | Yes | Identifies the [workspace](Workspace.md) that owns the source. |
| Status | Yes | Lifecycle enum. Create as `ACTIVE`. An admitted sync transitions to `SYNCING` and returns to `ACTIVE` at completion or abort. Reads during sync carry the [freshness flag](SpecSourceService.md#reads-and-writes-during-sync). Deletion transitions from `ACTIVE` to `MARKED_FOR_DELETION`, which permits only deletion progress as a mutation. |
| SpecType | Yes | Defines the accepted location and selection data. |
| Source root | Yes | String identifying the upstream location. The selected SpecType defines its accepted syntax, validates the supplied string and interprets its value. |
| Subfolder | No | Selector within the root. Its syntax and interpretation belong to the SpecType. Preserve the supplied expression for type-specific validation, for example `/spec` within a monorepo or a page subtree within a Confluence space. |
| Selection rules | Yes | Determine which items to include within the configured scope. |

### OriginalSpec

An OriginalSpec stores the source ID, locator and metadata. The original content stays upstream and is retrieved through that reference.

| Field | Required | Meaning |
| --- | --- | --- |
| Source ID | Yes | ID of the SpecSource used to read the item. |
| Item identity | Yes | The normalized path relative to the effective spec root is the sole identity within a source. The source supplies the lookup scope. A content hash identifies an observation of the item. |
| Current locator | Yes | Relative path used to retrieve this observation. |
| Input format | Yes | Upstream format of the item, which may be binary. |
| Original content hash | After a successful read | Hash of the exact retrieved bytes for a file or of the SpecType’s stable serialization for other content. |
| Upstream revision | When available | Upstream revision of the content actually retrieved. |
| Brief metadata | Yes | Short plain-text description for an LLM giving the item’s title or name and its origin, such as “Release checklist from the Platform team wiki.” When the title is unavailable, use the item locator and source type. |
| `processingStatus` | Yes | State of the observed item. See the transitions below. |

A new or changed reference is `PENDING`. The feature that continues processing marks that version `DONE` on success or `FAILED` on a processing error. A confirmed missing item or an item known to be inaccessible is `UNAVAILABLE`. Rediscovery after unavailability or an explicit processing retry returns the item to `PENDING`. A completion for an older observation cannot overwrite the state of a newer one.

### RawSpec

| Field | Required | Meaning |
| --- | --- | --- |
| RawSpec UUID | Yes | Stable identifier assigned on first successful publication for a source item. |
| Source ID | Yes | Direct reference to the originating SpecSource. It matches the OriginalSpec’s source ID. Multiple RawSpecs can share one source. |
| Item identity | Yes | Identity from the OriginalSpec. Together with the source ID, it identifies the upstream item. |
| Current locator | Yes | Upstream location recorded on the OriginalSpec at retrieval. |
| Input format | Yes | OriginalSpec’s upstream content format. |
| Original content hash | Yes | OriginalSpec’s hash of the retrieved content. |
| Upstream revision | When available | Revision recorded on the OriginalSpec. |
| Deletion state | Yes | Active or soft-deleted. Retain the last live publication for [rollback](SpecSourceService.md#removal-and-visibility). |
| Normalized text | Yes | LLM-readable text that preserves available document structure or chat message order. |
| Output format | Yes | Format of the normalized text, preferably Markdown or plain text. |
| Brief metadata | Yes | Description carried forward from the OriginalSpec, kept separate from the normalized text. |

## SpecSource management

Every operation requires the acting user’s UUID. Core verifies workspace ownership. A source in another user’s workspace has the same lookup outcome as a missing source.

| Operation | Behavior |
| --- | --- |
| Create | Verify that the [user](User.md) exists and owns the supplied workspace. Validate the source configuration for its SpecType and assign a stable source ID. |
| Get | Return the source configuration by source ID. |
| List | Return the sources of the supplied workspace, including an empty list when applicable. |
| Update | Validate changes to the root, subfolder or selection rules. Preserve omitted fields and the source ID. Changing the root is supported but discouraged because an existing relative locator may identify a different file. Reconcile observations against the new scope before reusing processing results. |
| Delete | Mark the source and perform the [ordered deletion](SpecSourceService.md#source-deletion) before removing its registration. Source management never modifies the upstream files. |

## Synchronizing OriginalSpecs

Sync discovers the current selection and returns one observation per detected change in content, provenance or selection, including newly discovered files. Compare source ID, relative locator and original content hash against the previous successful observation. A new path is an addition. Different bytes at the same path are an update. Identical bytes preserve the processing state on repeated scans. A rename appears as a disappearance and an addition when the new path has no prior publication.

Report confirmed disappearance separately from exclusion by changed selection rules. If discovery cannot cover the full selection, fail the sync before applying observations. Report individual read failures with the affected locator and reason. Record only verified observations as the new comparison baseline. The [DocumentProcessor](DocumentProcessor.md) transforms a file supplied for an OriginalSpec. Normalization follows conversion.

## SpecProvider

A [SpecSource](#specsource) stores one workspace’s configuration. A `SpecProvider` is the outgoing port through which core validates that configuration, discovers upstream items and reads their content. Each provider serves one SpecType. Register its CDI bean with an explicit `@Named` value matching the stable, case-sensitive type identifier. Core uses that name to resolve the provider. Core checks workspace ownership before discovery and reads.

| Operation | Provider contract |
| --- | --- |
| `describeConfiguration()` | Return a nonempty Markdown guide with required and optional settings, root and subfolder syntax, selection rules and an example. This operation takes no arguments and works before source creation, without an upstream connection. |
| `validate(source)` | Check the proposed source configuration against the type’s requirements on creation or update. Reject invalid settings. |
| `discover(source, known)` | Apply the current selection rules within the root and optional subfolder. Use previously known items to account for paths that have left the selection. Return a `Discovery` with item observations and issues after covering the full selection. Raise DISCOVERY_FAILED if the scan cannot cover the full selection. |
| `read(source, observed)` | Reassess the same source and item identity at read time. A successful `Read` supplies a stable file containing the observed version. Throw a reason-coded read exception if the content cannot be retrieved or its version has changed. Core passes the file to the [DocumentProcessor](DocumentProcessor.md) and closes the read handle after conversion. |

Discovery omits unchanged items, including items that remain missing or excluded. Each observation carries the OriginalSpec, the upstream condition, and a reason for an unsuccessful assessment. Use `NEW` for additions, `CHANGED` for changed or reselected files, `EXCLUDED` when a rule excludes it, `MISSING` for confirmed absence, `ACCESS_DENIED` for a known permission failure and `UNRESOLVED` when the provider cannot establish the outcome. A complete scan can yield an empty selection. Omission alone leaves an item’s status unknown.

For example, the `LOCAL_FILES` configuration with root `file:///work/repo` and subfolder `spec` reads selection rules from `/work/repo/spec/.specignore`. A line containing `/drafts/` excludes the top-level drafts directory. If a previous scan selected `drafts/order.md`, the next scan reports that known path as `EXCLUDED`. A regular file at `api/order.md` remains selected under this rule. If its bytes change between discovery and retrieval, the read fails with `VERSION_CHANGED`. A later discovery can report the new version.

The [LOCAL_FILES integration](../integrations/filesystem/LocalFiles.md) defines filesystem discovery and reads. The [post-MVP sync and history proposal](../todo/SpecSource.md) describes how core can use these observations and the resulting RawSpec.
