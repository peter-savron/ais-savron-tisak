# Spec CLI

The CLI calls a workspace’s configured [SpecSource](../../features/Spec.md#specsource) a `spec`. All six actions operate on that source through [SpecService](../../features/SpecService.md#operations). A spec name identifies one source within the selected workspace.

| Option | Behavior |
| --- | --- |
| `-u`, `--user <name>` | Selects the logged-in acting user for this invocation. |
| `-w`, `--workspace <name>` | Selects one of the acting user’s workspaces. |
| `-n`, `--name <name>` | Supplies the new spec name on create or selects an existing spec on get, update, delete or sync. |
| `-I`, `--no-interactive` | Skips prompts. Required inputs must resolve from options or applicable defaults. Available on every action except delete. |

Resolve the user first, then the workspace, then the spec. Explicit options take precedence over [session defaults](Session.md#common-session-lookup). If a usable session supplies no workspace, offer an interactive workspace selector or require `--workspace` in noninteractive mode. Validate workspace access for the resolved user. Pass UUIDs internally and use names in prompts, results and errors.

| Command | Behavior |
| --- | --- |
| `spec create` | Accepts `--name`, `--type <spec-type>`, `--root <value>` and optional `--subfolder <selector>`. Default the type to LOCAL_FILES. Prompt for a missing name or root and offer the optional subfolder. In noninteractive mode require name and root. Report the created spec name and root returned by the source type. |
| `spec update` | Select by name. Accept `--root <value>`, `--subfolder <selector>` or `--clear-subfolder`. The last two options are mutually exclusive. Preserve omitted fields. With no edit options, prompt using current values and offer an explicit choice to clear the subfolder. In noninteractive mode require a name and an edit option. Report the updated scope. |
| `spec delete` | Select by name. Following the [Workspace CLI](Workspace.md), show the workspace and spec names in a hidden-input password prompt, or accept `--password <password>`. Verify the acting user’s password before starting or resuming deletion. Report success after all cleanup phases complete. If cleanup stops, identify the marked spec and tell the user to retry the same command. |
| `spec get` | Display the selected source’s name, workspace, lifecycle status, type, root returned by the source type, optional subfolder and applicable selection rules. |
| `spec list` | List sources in the selected workspace by name, type and lifecycle status, including those awaiting deletion. An empty workspace produces an empty result. |
| `spec sync` | Select one active source and invoke sync. Display scan completeness, counts by change extent and each changed item’s source locator and extent from the [sync result](../../features/SpecService.md#specsyncresult). For a move show both locators. Include the reason for a failure or incomplete scan, and distinguish an unchanged scan from a partial one. |

For get, update, delete and sync, prompt for a missing spec name in interactive mode. Offer active sources for update and sync. Get and delete also accept sources marked for deletion. Reject an inaccessible or unknown explicit selector. Cancelling a prompt abandons the action.

For flags and prompts alike, pass the root and optional subfolder to core exactly as supplied. The selected SpecType parses and validates both strings. Surface its validation errors to the user. LOCAL_FILES accepts a file URI such as `file:///work/repo` and interprets the selector as a relative directory. Selection comes from the source’s `.ignore` file. Changing a root can make an existing locator point to different content; show the old and new roots when reporting the change. [LOCAL_FILES specification](../../integrations/filesystem/LocalFiles.md)
