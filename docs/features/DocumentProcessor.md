# DocumentProcessor

DocumentProcessor transforms a file corresponding to an [OriginalSpec](Spec.md#originalspec) into text. The caller supplies the OriginalSpec for provenance and the readable file for the observed version. The caller owns the file and keeps its contents stable through conversion. The processor, file type determiner and converters may open and close their own read handles. File creation, replacement and cleanup belong to the caller.

## Interfaces

| Operation | Inputs | Result |
| --- | --- | --- |
| Determine file type | A readable file | Return the detected MIME type as a type/subtype string. An unrecognized readable file yields `application/octet-stream`. A missing or unreadable file produces a read failure. |
| Process document | OriginalSpec, file and caller-owned output stream | Determine the file’s MIME type, select the highest-priority converter accepting that type and write its transformed text to the destination in buffered chunks. Return success when all output has been flushed, or report an error. |
| Convert | OriginalSpec, file and output destination | Each converter declares which MIME types it accepts and its selection priority. The selected converter writes the complete transformed text. If none accepts the detected type, report unsupported format. A selected converter’s failure is a processing error. |

## Output and ownership

Use a caller-supplied `OutputStream` as the result destination. A returned output stream would give the consumer a writing interface when it needs to read the result. Supplying the destination allows a single sequential pass with bounded buffering and avoids creating, rereading and deleting a temporary output file for every conversion. A caller needing replay or random access can supply a file-backed destination.

The processor flushes its buffers on success and leaves the destination open. If reading, conversion, writing or flushing fails, the caller discards any partial result. Normalization and publication as a RawSpec follow successful conversion.

See the [MIME detection implementation](../integrations/document-processing/FileTypeDeterminer.md) and the [plain-text fallback converter](../integrations/document-processing/PlainTextConverter.md).
