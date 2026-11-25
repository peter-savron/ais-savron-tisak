# AppContext

`AppContext` is a shared input for core operations across features. It identifies who is using the application and where in the application they are operating.

| Field | Definition |
| --- | --- |
| `userId` | UUID of the acting [user](User.md). |
| `context` | The current application context in which the operation is invoked. |

The calling API resolves the current context and passes it to core operations that require it. Each core feature uses the relevant fields to apply its own business and authorization rules. The shared model can be extended as features need more context.

[Workspace](Workspace.md#access) is one consumer. Its core uses the acting user ID to enforce owner-only access.
