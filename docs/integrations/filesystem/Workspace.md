# Workspace filesystem storage

On workspace creation, create a directory inside the owner's existing user directory at `<user-directory>/workspaces/<workspace-uuid>/`. Use the application UUID so the path stays stable when the workspace code name changes. Components manage their own files within the workspace directory.

Report creation as successful once both the directory and the [ArcadeDB record](../arcadedb/Workspace.md) exist. If creation fails, clean up any empty directory or record created by the failed attempt.

On workspace deletion, remove the directory once component cleanup is complete. Remove only the empty workspace directory. If files remain, report that deletion is blocked and preserve the workspace record and session references.

If the directory was removed and the ArcadeDB deletion transaction fails, restore the empty directory so the workspace can be used or deletion retried.
