# Document mapping integrations

Document mapping takes an OriginalSpec retrieved by a [SpecType implementation](../spec-source/README.md) and extracts LLM-readable text. Normalization then produces the stored [RawSpec](../../SpecSource.md) text. Markdown and plain text are the preferred output formats. Text already in one of those formats can be read directly. The initial extraction path for scanned or image-based documents uses OCR.

The OCR integration takes document content and emits text in page and reading order. Preserve recognizable headings, paragraphs, and lists in Markdown where the available layout permits. Report extraction failures and incomplete results to the sync operation. Incomplete results leave the last successful snapshot available for retry.

Normalize after mapping and before diffing. Apply consistent line endings, trim incidental spaces, and collapse extra blank lines where they carry no meaning. Preserve meaningful Markdown indentation, fenced code, and intentional breaks. Record the mapping method and the mapping and normalization versions with the snapshot. The OCR engine, supported input formats, and handling of low-confidence text are open decisions.
