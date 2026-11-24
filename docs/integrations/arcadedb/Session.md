# Session storage in ArcadeDB

Store [sessions](../../features/Session.md) as documents of type `Session`. Each document has a required, unique string `id` with a unique index for direct lookup across the installation. The value is the invoking shell’s decimal PID, the session name, or the reserved literal `_global`. Use this application identifier for repository lookups. ArcadeDB assigns its own internal record identifier.

Store `userId`, optional `workspaceId`, optional `usesSession`, `lastAccessedAt`, and `expiresAt` as document attributes. Terminal documents also store shell-instance metadata to detect PID reuse. A workspace-less session uses the same document type and index as any other session. Setting or changing the workspace updates `workspaceId` on the existing document. Workspace deletion removes sessions that reference that workspace.

## Expiry and concurrent changes

Every Session repository call, including reads and listings, first deletes all documents at or past their `expiresAt` and clears matching terminal references. Cleanup persists even when the requested operation cannot proceed. Coordinate cleanup with refresh and identifier reuse so it cannot delete a refreshed or replacement session.

Make expiry checks and session changes atomic with respect to competing operations on the same identifier. A refresh updates `lastAccessedAt` and `expiresAt` together. When switching sessions, update the terminal’s `usesSession` and the affected timestamps atomically. Listing candidates reads stored timestamps without refreshing them.

Named session deletion removes the document and clears matching terminal references as one consistent operation. Clearing a reference preserves the terminal’s workspace, user, last-access timestamp, and expiry. Serialize deletion with switches to the same target and recreation of the same name.

See the [Session CLI specification](../../api/cli/Session.md) for command behavior.
