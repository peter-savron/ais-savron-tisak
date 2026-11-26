# Workspace filesystem storage

On workspace creation, create a directory inside the owner's existing user directory at `<user-directory>/workspaces/<workspace-name>/`. Use the workspace code name as the directory component and reject collisions under filesystem name comparison. Coordinate a code-name change with the directory rename and database update so component paths remain workspace-relative. Existing UUID-named directories must be moved or reconciled before using the named layout; preserve their contents on collision or failure. Components manage their own files within the workspace directory.

Report creation as successful once both the directory and the [ArcadeDB record](../arcadedb/Workspace.md) exist. If creation fails, clean up any empty directory or record created by the failed attempt.

On workspace deletion, remove the directory once component cleanup is complete. Remove only the empty workspace directory. If files remain, report that deletion is blocked and preserve the workspace record and session references.

If the directory was removed and the ArcadeDB deletion transaction fails, restore the empty directory so the workspace can be used or deletion retried.
