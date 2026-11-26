# Workspace

A workspace groups components from different parts of the app under one owner. Workspace operations manage the container and its metadata. Each component defines its own data, operations, and behavior within that scope.

[Feature](Feature.md) owns feature hierarchies, [Secret](Secret.md) owns workspace-scoped secret operations, and [Functionality](Functionality.md) defines behavior reusable across workspaces. [Session](Session.md) owns the selection of a workspace as a command default.

## Model

| Field | Definition |
| --- | --- |
| `id` | Stable UUID used for direct lookup. |
| `name` | Required code name following the shared [name identifier rules](NameIdentifier.md). It is unique among workspaces with the same owner. |
| `ownerId` | UUID of the [user](User.md) who owns the workspace. |
| `title` | Optional display title. |
| `description` | Optional description of up to 1,000 characters, including newlines. |

## Access

Every workspace operation requires the acting user's UUID. The workspace core uses it to authorize the operation. The acting user becomes the owner at creation and is the only user who can retrieve, update, or delete the workspace. Listings and name lookups are scoped to that user. For operations targeting a workspace by ID, the core verifies ownership before returning or changing it. A workspace owned by another user produces the same result as a missing workspace.

## Operations

| Operation | Behavior |
| --- | --- |
| Create | Creates a workspace for the acting user. Validates the name and rejects a duplicate within that user's workspaces. Creation succeeds once the workspace and its [RawSpecIndex](RawSpecIndex.md#initialization-and-changes) have been initialized. |
| Update | Changes any supplied combination of name, title, and description. Omitted fields retain their values. A name change follows the same validation and uniqueness rules as creation. |
| Delete | Removes the workspace after any remaining components have been handled through their own lifecycle rules. If a component still requires the workspace, reject deletion. |
| Get by ID | Retrieves an owned workspace by its UUID. |
| Get by name | Retrieves a workspace by its exact name within the acting user's workspaces. |
| List | Returns all workspaces owned by the acting user. Returns an empty list when the user owns none. |

See the [CLI specification](../api/cli/Workspace.md) [ArcadeDB storage specification](../integrations/arcadedb/Workspace.md), and [filesystem storage specification](../integrations/filesystem/Workspace.md).
