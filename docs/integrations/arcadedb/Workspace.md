# Workspace storage in ArcadeDB

Store [workspaces](../../features/Workspace.md) as `Workspace` documents with `id`, `ownerId`, `name`, and optional `title` and `description`. Use the application UUID for `id` and the owner's user UUID for `ownerId`.

Use a unique index on `id` and a case-sensitive unique composite index on `(ownerId, name)`. Support lookup by application UUID and lookup and listing by the owner ID supplied by the core. The [workspace core](../../features/Workspace.md#access) makes authorization decisions. Enforce uniqueness during creation and rename, including concurrent writes, and roll back the entire mutation if it fails.

When deleting a workspace, follow the existing [session storage cleanup rules](Session.md) for sessions that reference it. Apply the workspace document removal and session cleanup in one transaction. See the [filesystem specification](../filesystem/Workspace.md) for the workspace directory lifecycle.

## Graph modeling

The current operations use indexed UUID and owner/name lookups. A component that only needs workspace membership can store a workspace UUID and use an indexed lookup.

Use a `Workspace` vertex if workspace-to-component relationships need to be traversed as graph edges. ArcadeDB vertices hold properties, so the workspace metadata and application UUID can live on the vertex. Keep the owner/name index for direct lookups and apply authorization in the core. [ArcadeDB graph model](https://docs.arcadedb.com/arcadedb/concepts/graphs)

A workspace becomes a supernode when it accumulates many edges or gains them rapidly. If the graph model is adopted, evaluate edge counts and concurrent writes for representative workspaces. Keep component deletion and session cleanup explicit in the relevant storage contracts. [ArcadeDB supernode documentation](https://docs.arcadedb.com/arcadedb/reference/supernodes)
