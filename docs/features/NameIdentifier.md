# Name identifier

[User names](User.md), [named session names](Session.md), and [workspace code names](Workspace.md) share this format.

A name identifier contains 1 to 64 ASCII characters. It starts with an ASCII letter. Subsequent characters may be ASCII letters, digits, underscores, or hyphens. The full value must match `[A-Za-z][A-Za-z0-9_-]{0,63}`. Names are case-sensitive.

Each feature defines its own uniqueness scope. The reserved global session identifier and terminal session PIDs follow the [session rules](Session.md#model).
