# Workspace

## Fields

- `id`
- `name` unique across User workspaces
- `owner` [User](User.md#User) who owns the workspace
- `context` workspace-specific context (short info about the company owning it, generic info, instructions for agents, etc.)
- `config` whatever config could be applied to the whole workspace (will be expanded later)
  - `language` localization to be used for the workspace (optional)
- `secrets` workspace-wide secrets
- `security` security configurations (URL blacklist/whitelist, tool usage permissions)
- `imports` what data from other workspaces it imports
- `exports` what data from the workspace it exports

## Actions

### Create workspace

### Update workspace

Using flags, you specify what you would like to update.

### Delete workspace

Prompts for confirmation.

### Use workspace

Can be flagged to be user-session wide or terminal session wide, unless flagged differently all commands.
