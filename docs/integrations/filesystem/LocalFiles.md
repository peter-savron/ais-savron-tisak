# LOCAL_FILES

This [SpecSource](../../features/Spec.md) discovers local file references and reads their current contents.

## Configuration description

The Markdown returned by `describeConfiguration()` documents the following settings.

- **Root** (required). Supply an absolute file URI such as `file:///work/repo`.
- **Subfolder** (optional). Supply a directory relative to the root, such as `spec`. Together these example values select `/work/repo/spec`. Omit the subfolder to select the root itself.
- **Selection**. The source scans regular files recursively and skips symbolic links. Put optional exclusion rules in `.specignore` at the effective spec root.

## Paths and retrieval

Resolve the absolute file URI to a normalized absolute local directory path at creation and whenever the root is updated. The effective spec root is that directory plus the optional subfolder. Store each OriginalSpec locator relative to the effective spec root and use it as the source-scoped item identity. For source root `file:///work/repo`, subfolder `spec` and file `/work/repo/spec/api/orders.md`, the locator is `api/orders.md`.

To get a file, resolve its relative locator against the effective spec root and read the resulting absolute path directly. Reject locators that are absolute or resolve outside that root. Never follow a symbolic link, including in a parent path, the configured root or subfolder, or the final file. Apply the same rule at open time and during discovery. Apply the current selection rules before returning bytes. An ignored path is excluded even if it exists. For a selected path, verify the bytes against the discovery reference and return them through a stable read handle. Throw `VERSION_CHANGED` on a mismatch. Throw a reason-coded read exception for missing, excluded, access denied or unresolved paths.

## Recursive discovery and sync

Walk the effective spec root recursively. Check each selected regular file, including hidden files, binary files and files without extensions. Skip symbolic links to files and directories. A previously selected regular file replaced by a link is now excluded. Each detected change yields one observation. Its reference carries the source ID and relative locator. Sync hashes the file bytes to detect edits, including same-size edits, while the returned reference stays independent of the payload. Reading file content for downstream processing is a separate operation.

Read the optional `.specignore` in the effective spec root on each scan if it is a regular file. A nested regular file named `.specignore` is an ordinary candidate and supplies no selection rules. A symlink named `.specignore` is skipped. With no rules, every regular file is selected, including the control file itself when present. Apply the last matching rule, including negation, root anchoring, directory patterns, wildcards, escaping and Git’s rule for excluded parent directories. A repository and its Git configuration are unnecessary. The matching behavior follows the [Git pattern format](https://git-scm.com/docs/gitignore#_pattern_format). A permissively licensed implementation is available in [ripgrep’s ignore crate](https://github.com/BurntSushi/ripgrep/tree/master/crates/ignore).

If a directory or its applicable ignore rules cannot be read, fail discovery with the affected scope and underlying cause. A failed scan cannot establish deletion. A newly ignored path leaves the selection even if the file still exists.

For each sync, scan the entire effective spec root under the current ignore rules. Reconcile the union of observed and stored paths, including tombstones. Return observations only for new paths and changes in content, provenance or selection. Report coverage errors separately. Distinguish confirmed exclusion from confirmed absence and unknown existence under an unreadable or pruned subtree. Compare hashes for selected files present on both sides. Apply the [publication and restoration rules](../../features/SpecSourceService.md#removal-and-visibility) to the resulting changes.
