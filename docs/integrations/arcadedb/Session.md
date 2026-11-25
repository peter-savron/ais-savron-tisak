# Session storage in ArcadeDB

Store [sessions](../../features/Session.md) as documents of type `Session`. Start assigns each stored slot a unique string `id` with a unique index for direct lookup across the installation. The value is the invoking shell’s decimal PID, the session name, or the reserved literal `_global`. Use this application identifier for repository lookups. ArcadeDB assigns its own internal record identifier.

Every session document requires `userId`, `lastAccessedAt`, and `expiresAt`. Store `workspaceId` and `usesSession` when present. Terminal documents store shell-instance metadata when available. On confirmed PID reuse, replace the old terminal document and its preferences in the same transaction as a successful Start. A workspace-less session uses the same document type and index as any other session. Setting or changing the workspace updates `workspaceId` on the existing document. Workspace deletion removes sessions that reference that workspace.

Only persist state that meets the [Session feature’s cross-invocation rule](../../features/Session.md). Session documents must exclude chat IDs and invocation-specific state.

## Expiry and concurrent changes

Every Session repository call, including reads and listings, first deletes all documents at or past their `expiresAt` and clears matching terminal references. Cleanup persists even when the requested operation cannot proceed. Coordinate cleanup with refresh and identifier reuse so it cannot delete a refreshed or replacement session.

Make expiry checks and session changes atomic with respect to competing operations on the same identifier. A refresh updates `lastAccessedAt` and `expiresAt` together. When switching sessions, update the terminal’s `usesSession` and the affected timestamps atomically. Listing candidates reads stored timestamps without refreshing them.

Whenever a named or global slot is removed, including by Stop, expiry, or workspace deletion, remove the document and clear matching terminal references as one consistent operation. Clearing a reference preserves the terminal’s workspace, user, last-access timestamp, and expiry. Serialize deletion with switches to the same target and recreation of the same name.

See the [Session CLI specification](../../api/cli/Session.md) for command behavior.
