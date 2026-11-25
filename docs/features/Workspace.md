# Workspace

A workspace groups components from different parts of the app under one owner.

## Model

| Field | Definition |
| --- | --- |
| `id` | Stable UUID used for direct lookup. |
| `name` | Required code name following the shared [name identifier rules](NameIdentifier.md). It is unique among workspaces with the same owner. |
| `ownerId` | UUID of the [user](User.md) who owns the workspace. |
| `title` | Optional display title. |
| `description` | Optional description of up to 1,000 characters, including newlines. |

## Access

Every operation requires [AppContext](AppContext.md). The workspace core authorizes each operation using the acting user ID in that context. The acting user becomes the owner at creation and is the only user who can retrieve, update, or delete the workspace. Listings and name lookups are scoped to that user. For operations targeting a workspace by ID, the core verifies ownership before returning or changing it. A workspace owned by another user produces the same result as a missing workspace.

## Operations

| Operation | Behavior |
| --- | --- |
| Create | Creates a workspace for the acting user. Validates the name and rejects a duplicate within that user's workspaces. |
| Update | Changes any supplied combination of name, title, and description. Omitted fields retain their values. A name change follows the same validation and uniqueness rules as creation. |
| Delete | Removes the workspace. |
| Get by ID | Retrieves an owned workspace by its UUID. |
| Get by name | Retrieves a workspace by its exact name within the acting user's workspaces. |
| List | Returns all workspaces owned by the acting user. Returns an empty list when the user owns none. |

A [session](Session.md) can select an accessible workspace as a command default.

See the [CLI specification](../api/cli/Workspace.md) and [ArcadeDB storage specification](../integrations/arcadedb/Workspace.md).
