# DocumentProcessor

DocumentProcessor is a deployment-agnostic utility that transforms a readable file into normalized text. It receives the input `Path` and a caller-owned output destination; it does not receive or mutate a Spec. The caller keeps the input contents stable through conversion and owns creation and cleanup of any temporary files. The processor, file type determiner and converters may open and close their own read handles.

## Interfaces

| Operation | Inputs | Result |
| --- | --- | --- |
| Determine file type | A readable file | Return the detected MIME type as a type/subtype string. An unrecognized readable file yields `application/octet-stream`. A missing or unreadable file produces a read failure. |
| Process document | Input `Path` and caller-owned output stream | Determine the file’s MIME type, select the highest-priority converter accepting that type and write its transformed text to the destination in buffered chunks. Return the output MIME type after all output has been flushed. |
| Convert | Input `Path` and output destination | Each converter declares which MIME types it accepts and its selection priority. The selected converter writes the complete transformed text and returns its output MIME type. |

## Output and ownership

Use a caller-supplied `OutputStream` as the result destination. A returned output stream would give the consumer a writing interface when it needs to read the result. Supplying the destination allows a single sequential pass with bounded buffering. The caller may provide a file-backed destination when it needs replay or random access and remains responsible for the file's lifecycle.

The destination receives the converted text bytes. The returned string is the output MIME type for those bytes, for example `text/plain` or `text/markdown`. The caller records it as the [published output format](Spec.md#publishedversion). The processor flushes its buffers on success and leaves the destination open. The caller maintains Spec provenance, including the input MIME type.

## Errors

The processor reports unsupported input and document processing failures through the checked `DocumentProcessingException`. Unsupported input occurs when no converter accepts the detected MIME type. When detection, conversion, writing or flushing fails with an I/O error, the processor preserves that error as the exception cause. The caller discards partial output on failure and proceeds to RawSpec publication only after successful processing.

See the [MIME detection implementation](../integrations/document-processing/FileTypeDeterminer.md) and the [plain-text fallback converter](../integrations/document-processing/PlainTextConverter.md).
