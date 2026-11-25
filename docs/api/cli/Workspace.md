# Workspace CLI

The [workspace feature](../../features/Workspace.md) defines fields and owner access.

| Option | Behavior |
| --- | --- |
| `-n`, `--name <name>` | Supplies the code name on create or selects an existing workspace by name. |
| `-u`, `--user <name>` | Uses the specified logged-in user for this invocation. When omitted, resolve the user through the [session defaults](Session.md#common-session-lookup). |
| `-I`, `--no-interactive` | Skips prompts. Required selectors and values must be supplied as options. |

Resolve the user before looking up a workspace. Pass the resolved user UUID and CLI context in [AppContext](../../features/AppContext.md) to the workspace core, which authorizes the operation.

| Command | Behavior |
| --- | --- |
| `workspace create` | Prompts for a code name when omitted. Offers an optional single-line title prompt and an optional [multiline description prompt](LongTextPrompt.md). `--title` and `--description` supply those values directly. In noninteractive mode, require `--name` and leave omitted optional fields unset. |
| `workspace update` | Selects the workspace by name, then updates its metadata. Use `--new-name` to rename it, and `--title` or `--description` to set those fields. If no edit options are given, prompt for the three fields and let the user keep the current values. In noninteractive mode, require a selector and at least one edit option. |
| `workspace delete` | Selects the workspace by name and asks for confirmation. Noninteractive deletion requires an explicit `--name` and `--yes`. |
| `workspace get` | Displays one workspace selected by name. |
| `workspace list` | Lists the acting user's workspaces. |

When get, update, or delete has no selector in interactive mode, offer a selection from the acting user's workspaces. Cancelling any prompt abandons the requested action. Configure the description prompt with the workspace limit of 1,000 characters. Apply the same limit to values supplied by flag.
