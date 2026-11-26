# LOCAL_FILES

This [SpecSource](../../features/Spec.md) discovers local file references and reads their current contents.

## Paths and retrieval

Resolve the absolute file URI to a normalized absolute local directory path at creation and whenever the root is updated. The effective spec root is that directory plus the optional subfolder. Store each OriginalSpec locator relative to the effective spec root and use it as the source-scoped item identity. For source root `file:///work/repo`, subfolder `spec` and file `/work/repo/spec/api/orders.md`, the locator is `api/orders.md`.

To get a file, resolve its relative locator against the effective spec root and read the resulting absolute path directly. Reject locators that are absolute or resolve outside that root. Apply the current selection rules before returning bytes. An ignored path is excluded even if it exists. For a selected path return the current bytes and their hash. If the file has changed since discovery, identify the version actually read. Report missing, access denied or unresolved according to the filesystem outcome.

## Recursive discovery and sync

Walk the effective spec root recursively. Return one OriginalSpec reference per selected regular file, including hidden files, binary files and files without extensions. Directories organize traversal. The reference carries the source ID and relative locator. Sync hashes the file bytes to detect edits, including same-size edits, while the returned reference stays independent of the payload. Reading file content for downstream processing is a separate operation.

Read the optional `.ignore` in the effective spec root on each scan. With no rules, every regular file is selected, including the control file itself when present. Apply the last matching rule, including negation, root anchoring, directory patterns, wildcards, escaping and Git’s rule for excluded parent directories. A repository and its Git configuration are unnecessary. The matching behavior follows the [Git pattern format](https://git-scm.com/docs/gitignore#_pattern_format). A permissively licensed implementation is available in [ripgrep’s ignore crate](https://github.com/BurntSushi/ripgrep/tree/master/crates/ignore).

If a directory or its applicable ignore rules cannot be read, report incomplete coverage for the affected scope. A failed scan cannot establish deletion. A newly ignored path leaves the selection even if the file still exists.

For each sync, scan the entire effective spec root under the current ignore rules. Reconcile the union of observed and stored paths, including tombstones. Return the current and previous existence/selection observations and available byte hashes per path, plus coverage errors. Distinguish confirmed exclusion from confirmed absence and unknown existence under an unreadable or pruned subtree. Compare hashes for selected files present on both sides. Apply the [publication and restoration rules](../../features/SpecService.md#removal-and-visibility) to the resulting changes.

## Open decisions

- Should nested `.ignore` files supplement the root file?
- Should symlinks to files or directories inside the effective root participate? Reads and traversal must stay within the root.
