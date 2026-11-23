# Session

In carious deployment modes we would like to preserve some context between retries, so that user does not have to specify which User, Workspace or Feature to use.

Session context lets callers reuse a user, workspace, or feature across invocations.

## CLI

Session is preserved via invoking terminal PID, there can also be one global session configured and also named sessions.

The invoking shell can supply a user name with `BISTRA_SESSION_USER` for commands that support a session default. See the [User CLI specification](../api/cli/User.md#user-name) for which commands use it.
