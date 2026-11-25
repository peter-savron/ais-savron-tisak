# Session CLI

The [Session feature](../../features/Session.md) defines identifiers, validation, and expiry.

Session defaults carry reusable preferences across commands. Commands that need a chat ID obtain it outside the session.

## Session selector

The `start`, `stop`, `set`, and `get` actions accept either `--global` or `--name <session-name>`. With neither option, `start`, `stop`, and `set` target the current terminal slot. `get` uses the common lookup below. The options are mutually exclusive. These selectors apply to the invocation. Use `session use` to switch the session for subsequent commands.

## Short flags

| Short flag | Long flag | Value |
| --- | --- | --- |
| `-n` | `--name` | Session name |
| `-l` | `--local` | |
| `-g` | `--global` | |
| `-u` | `--user` | User name |
| `-w` | `--workspace` | Workspace name |

Each short flag is available on the same actions as its long form.

## Actions

| Command | Behavior |
| --- | --- |
| `session start [selector] [--user <name>] [--workspace <name>]` | Starts a terminal session by default. Use `--name` to create a named session or `--global` to start the global session. Prompts for the user if omitted. The workspace is optional and is left unset when omitted. Replaces a terminal slot on confirmed PID reuse. Otherwise reports an occupied active slot. |
| `session use [--local \| --global \| --name <session-name>]` | Switches the current shell to its own terminal session, the global session, or a named session. Requires an active terminal session. Selectors are mutually exclusive. With no selector, opens a selector of active, usable named sessions. Updates its `usesSession` while preserving its stored user and workspace. `--local` clears the reference. Reports the selected kind and identifier on success. |
| `session stop [selector]` | Stops the selected session. Reports an inactive session if the slot has no active record. |
| `session delete [--name <session-name>]` | Opens a selector of stored named sessions if the name is omitted. Deletes the named session if it exists after expiry cleanup. Terminals that referenced it resume using their own preferences. Reports a missing record if it is already absent. |
| `session set [selector] [--user <name>] [--workspace <name>]` | Updates an active session. With one option, preserves the other value. With neither option, interactively prompts for both, allowing Enter to keep each current value. At least one value must change. |
| `session get [selector]` | Prints the kind (`global`, `terminal`, or `named`), identifier, workspace name (or unset), user name, UTC last access and expiry after refresh, and `usesSession` (selected identifier or unset). Includes the linked session as a nested `linkedSession` object with the same display fields when present. Reports an inactive session if lookup finds no active record. |

## Names and prompts

The interactive selector shows session names and their stored last-access times, most recently accessed first. Ties are ordered by name ascending. If there are no eligible records, the CLI reports that the list is empty. If the chosen record is no longer eligible, the CLI reports the change and reloads the candidates. Opening or cancelling the selector does not refresh or select a session. Repository calls still perform expiry cleanup. In noninteractive mode, `use` requires a selector flag and `delete` requires `--name`.

Resolve the user name to a UUID first, then resolve a supplied workspace name among workspaces available to that user. When `set` changes only the user, validate access to the existing workspace by its UUID if a workspace is set. Reject invalid, unknown, inaccessible, or ambiguous values supplied as flags. At a prompt, explain the problem and allow re-entry. In noninteractive mode, `start` requires `--user` and `set` requires at least one. Cancellation and input errors leave the requested changes unapplied.

## Common session lookup

For `session get` and commands that need session defaults, apply the following order after expiry cleanup.

1. If a selector flag is supplied, use its session directly. `--name <name>` selects that named session and `--global` selects the global slot. An unavailable explicit selection is an error.
2. With no selector flag, select the active local session for the invoking PID and shell instance (when verifiable).
3. If no active local session matches, select `_global`.
4. If the global fallback is also inactive, report an inactive-session error.

After expiry cleanup, when the terminal session has `usesSession` set, retrieve the referenced named or global session by its identifier. Return the terminal session as the parent with its own fields intact, including `usesSession`, and attach the complete referenced session as a nested `linkedSession` object. Successful resolution refreshes both sessions. If a stored link cannot be resolved, report the referenced session as unavailable. For example, after `session use --name review`, `session get` shows the terminal session with `review` attached, and `session get --name review` shows `review` directly.

Before returning a session or using its defaults, check that its user is still logged in. If the result contains a linked session, check both users. If any required user is logged out, ask them to run `user login --name <user-name>` and retry. Leave the retrieved sessions’ last-access and expiry timestamps unchanged. Logout removes the user’s terminal slots. A surviving terminal linked to a retained session whose user is logged out reports that login is required.

Commands that support session defaults use explicit values first. For omitted values, use the linked session’s preferences when present, or the returned parent session’s preferences otherwise. If the effective session has no workspace and the command needs one, the user must supply it through the command’s workspace input. Validate workspace access for the effective user, including when the user was explicitly selected. Session mutations continue to target their designated slot directly.

For commands that already support `BISTRA_SESSION_USER`, the variable remains a user-name override between explicit user input and implicit session lookup. It does not populate the workspace or modify a session. See [User CLI](User.md#user-name) for the commands that accept a session user.
