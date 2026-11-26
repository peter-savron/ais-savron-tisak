# SpecSource integrations

Each SpecType will have its own integration spec in this folder. It will define the SpecSource data that the type accepts and how its implementation uses that data to interact with the original source. The [SpecSource core spec](../../SpecSource.md) defines RawSpec, sync behavior, snapshots, and version diffs.

| Candidate | Navigation and retrieval to specify |
| --- | --- |
| GitHub | Repository, branch or ref, root path, directory traversal, and file reads. |
| Confluence | Space or root page, child pages, page reads, and attachment scope. |
| Local folder | Root directory, recursive traversal, file selection, and file reads. |
| Chat history | Account or collection scope, conversation selection, and retrieval of messages in order. |

For each integration, define the configuration, how items are discovered and read, what identifies an item and its revision, and how incomplete discovery or confirmed removal is reported. Document any access or pagination requirements that affect retrieval. For document text extraction, see [Document mapping integrations](../document-mapping/README.md).

Each implementation returns an OriginalSpec with metadata for the content actually retrieved. Define the stable serialization used for source hashes when retrieval produces content without original file bytes.

Each SpecType interprets its upstream errors. A response equivalent to 401 or 403 indicates an access error. A response equivalent to 404 can indicate confirmed absence when the implementation can establish that meaning. A response that could also reflect restricted visibility stays unresolved unless the implementation can classify it more precisely.
