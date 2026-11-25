# LongTextPrompt

The [workspace CLI](Workspace.md) uses this interactive prompt to edit descriptions.

- Display an editable multiline input. Enter inserts a newline at the cursor. Ctrl+Enter confirms the current text.
- The caller can supply a character cap for each prompt. Count newlines and enforce the supplied cap for typing and paste.
- Show a live character counter below the input, updated after each edit. When a cap is supplied, show the current count and cap, such as `0 / 1000`. Otherwise, show the current count.
- Support cancellation and report it separately from confirming empty input. An optional prompt on creation starts empty. On update, prefill the current description so it can be kept, edited, or cleared.
