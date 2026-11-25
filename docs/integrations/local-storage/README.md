# Local storage

The `integration-local-storage` Maven module implements core repository ports for the local application. It owns filesystem storage and embedded ArcadeDB. Additional local storage backends can live in this module behind the relevant core ports. See the [Maven module boundaries](../../MavenModules.md).

## Current layout

Paths are relative to the data and cache roots resolved by `StorageConfig`.

| Root | Path | Contents |
| --- | --- | --- |
| Data | `.arcadedb/` | Embedded ArcadeDB, including user records, [session records](../arcadedb/Session.md), and schema changelog history. |
| Data | `<user-id>/` | Per-user directory named by UUID. |
| Cache | `.user-keys/<username>` | User key files managed by the filesystem key repository. |

The workspace storage design covers [ArcadeDB metadata](../arcadedb/Workspace.md) and [filesystem contents](../filesystem/Workspace.md).
