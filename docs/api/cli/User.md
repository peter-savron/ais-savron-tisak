# User CLI

## User name

- `user create`, `user login`, and `user delete` require an explicit user name. Supply it with `-n` or `--name`, or enter it when prompted. These commands always ask for a name when the option is omitted, even if the session has a user.
- The CLI resolves the name before asking for a password. Names must match the [User name format](../../features/User.md#fields). An invalid name entered at the prompt triggers another prompt. An invalid name passed as an option is rejected.
- `user logout` and `user get` use the [CLI session user](../../features/Session.md#cli) when no explicit selector is given. `user get --id` selects by UUID and is mutually exclusive with `--name`.
- `user list` needs no user selector. It appends `LOGGED` to each logged-in user’s row and displays it in green when ANSI color is enabled.

See the [User feature](../../features/User.md) for account and authentication behavior.
