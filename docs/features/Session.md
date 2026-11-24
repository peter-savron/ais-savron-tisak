# Session

A session provides the default [user](User.md) and [workspace](Workspace.md) for commands that support session context. It stores references to those records. Commands still apply their own authorization rules.

## Model

| Field | Definition |
| --- | --- |
| `id` | String identifier, unique within the local application installation among active sessions. |
| `workspaceId` | Optional UUID of the selected workspace. |
| `userId` | UUID of the user selected for the session. |
| `usesSession` | Optional string identifier of a named or global session whose preferences this terminal session uses. An unset value uses the terminal session’s own preferences. Global and named sessions leave this field unset. |
| `lastAccessedAt` | UTC instant when the session was created or last successfully used. |
| `expiresAt` | UTC instant when the session becomes inactive. |

| Kind | Identifier |
| --- | --- |
| Global | Reserved literal `_global`. There is one global slot per local installation. |
| Terminal | Decimal PID of the invoking terminal's shell. Invocations from the same shell use the same slot. |
| Named | Case-sensitive name matching the [user name format](User.md#fields), `[A-Za-z][A-Za-z0-9_-]{0,63}`. |

The identifier determines the kind. A terminal slot belongs to the shell instance that started it. If a PID is reused, the new shell cannot inherit the previous shell's session. The CLI supplies a shell instance identity to support this check.

## Lifecycle

- **Start** validates the identifier and requires a valid user. If a workspace is supplied, it validates the selected user’s access to it. It creates an active session with `expiresAt = now + the applicable idle timeout`. Starting an occupied active slot fails. A slot freed by expiry cleanup can be reused.
- **Get** looks up a session by its string identifier. The identifier can be the invoking terminal’s PID for the current terminal session, `_global` for the global session, or a session name. Before returning an active session, verify that its selected user is still logged in. If the user is logged out, report that login is required and leave the session timestamps unchanged. Otherwise, if the session is usable, refresh its expiry and return the updated session. A command that successfully resolves a session for its defaults also refreshes it, even if the command later fails for an unrelated reason.
- **Set** changes the user, the workspace, or both. It preserves any omitted value, validates the resulting pair, and refreshes expiry. Changing the workspace updates the workspace reference on the session with the same identifier.
- **Use** requires an active terminal session. Selecting a named or global session validates the target and stores its identifier in the terminal session’s `usesSession`. Selecting local clears `usesSession`. Both operations preserve the terminal’s stored `workspaceId` and `userId`. A successful switch refreshes the terminal and, when selected, the target using their respective timeouts. A failed switch leaves both records unchanged.
- **Resolve defaults** follows `usesSession` from an active terminal session and takes both preferences from the target. Verify that each retrieved session’s user is logged in before refreshing either session. Successful resolution refreshes both sessions. If the reference is unset, it uses and refreshes the terminal session alone. If the target is unavailable, it reports that the target is inactive. Get and Set operate on the identified record directly.
- **Stop** removes the active session and releases its identifier. Missing and expired sessions are reported as inactive. A session whose references have become invalid can still be stopped.

- **Delete** permanently removes a named session by name, if it still exists after expiry cleanup. It clears terminal references to that session so those terminals resume their own stored preferences. If the record is already absent, report it as missing.

Quarkus configuration supplies two independently configurable positive durations. Set either property in `application.properties` to override its default.

| Property | Default | Applies to |
| --- | --- | --- |
| `bistra.session.local.idle-timeout` | 24 hours (`PT24H`) | Terminal sessions |
| `bistra.session.named.idle-timeout` | 1 month (30 days, `P30D`) | Named sessions and the global session |

Start and each successful refresh set `lastAccessedAt` to the current time and `expiresAt` to that same instant plus the applicable duration. A session is inactive at or after `expiresAt` and cannot be refreshed or updated. Failed validation leaves the stored values and timestamps unchanged. A session with an unset workspace can supply a user default. A missing selected user or workspace, or loss of access to a selected workspace, prevents the session from supplying defaults. Every repository call deletes expired sessions before performing the requested operation.

Listing candidates for a selector reads their stored timestamps without refreshing them. Order candidates by `lastAccessedAt` descending, then by identifier ascending for ties. Revalidate the selected record when executing the action.

See the [ArcadeDB storage contract](../integrations/arcadedb/Session.md) for persistence and concurrency requirements.

## CLI

See the [Session CLI specification](../api/cli/Session.md) for selectors, name resolution, and defaults used by other commands.
