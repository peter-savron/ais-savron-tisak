# Session CLI usage

A session remembers the user and optional workspace for commands that use session defaults. Start a local session for your current shell, or create a named or global session that you can select from another shell.

## Start a local session

From the repository root, use the [CLI build and run instructions](../../../README.md#build-the-cli). The examples use the `./bistra` launcher and assume the user `alice` exists. See the [User CLI](../../api/cli/User.md) for user setup.

```sh
./bistra user login --name alice
./bistra session start --local --user alice
./bistra session get
```

Local is the default for `session start`. Run subsequent commands from the same shell to use that local session. In an interactive terminal, omit `--user` to be prompted for a user name.

## Switch to a named or global session

With a local session active, create and select a named session.

```sh
./bistra session start --name review --user alice
./bistra session use --name review
./bistra session get
```

`session get` shows the local session and its selected target under `linkedSession`. The `usesSession` field appears when the session has a link. Run `session get --name review` to inspect the named session directly.

To use a global session, run `session start --global --user alice` and then `session use --global`. Use `session get --global` to inspect it directly. With no selector, `session get` tries the local session first and falls back to the global session if the local slot is inactive.

Run `session use` in an interactive terminal to browse named sessions. Use the arrow keys to move and Enter to confirm. The list shows session names and their last access times, with the most recently accessed first. Run `session use --local` to return to the local session’s own preferences.

## Change the default user

Use `session set --user bob` to change the local session’s saved user, or `session set --name review --user bob` to change the named session. Use `--global` to edit the global session. If the local session is linked, edit the linked session explicitly to change the user supplied by that link.

Workspace selection by name is currently unavailable in the Session CLI. Omit `--workspace` when starting or editing a session. If the CLI reports that the selected user is logged out, run `user login --name <name>` and retry.

## Stop or delete a session

| To remove | Run |
| --- | --- |
| The local session in this shell | `./bistra session stop` |
| A named session | `./bistra session delete --name review` |
| The global session | `./bistra session delete --global` |

`session stop --name review` also stops a named session. In an interactive terminal, `session delete` opens the named-session selector. For scripts, provide `--name` or `--global` explicitly. Removing a named or global session clears links to it, so affected shells use their own saved preferences again.

For command details and lifecycle rules, see the [Session CLI specification](../../api/cli/Session.md) and [Session feature](../../features/Session.md).
