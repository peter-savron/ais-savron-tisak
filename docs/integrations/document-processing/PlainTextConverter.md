# Plain-text fallback converter

This [DocumentProcessor converter](../../features/DocumentProcessor.md#interfaces) accepts the base MIME type `text/plain`. It has the lowest selection priority. Every other eligible converter takes precedence. Unknown or binary MIME types remain unsupported unless a converter explicitly accepts them.

Copy the file bytes to the output stream in bounded chunks. Preserve the original encoding, byte order mark if present, line endings, whitespace and final newline exactly. An empty input produces empty output. The output MIME type remains `text/plain`. Read and write errors propagate to the processor so the caller can discard incomplete output.
