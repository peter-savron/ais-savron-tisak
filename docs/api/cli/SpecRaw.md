# RawSpec CLI

The `spec-raw` group addresses published text and its revisions. Resolve the user, workspace and source using the [spec-source CLI conventions](SpecSource.md). Select items by their source-scoped locator and pass the resolved RawSpec UUID internally. A get reads the published text even if upstream is offline.

| Command | Result |
| --- | --- |
| `spec-raw list` | List active publications in the selected source with their locators and current processing states. |
| `spec-raw get` | Return one active publication’s text, provenance and current item state. |
| `spec-raw history` | List an active publication’s revisions in order. |
| `spec-raw diff` | Compare two listed revisions of the selected publication and display the storage-produced differences. |
| `spec-raw restore` | Resolve a deleted item by source and last known locator for authorized rollback. Ordinary list, get, history and diff exclude tombstones. |

These operations use the [published RawSpec contracts](../../features/SpecSourceService.md#operations). Future graph construction and editing belong under `spec-graph`.

Reads during source sync show the potentiallyStale flag with committed results. Restore reports source busy while SYNCING.
