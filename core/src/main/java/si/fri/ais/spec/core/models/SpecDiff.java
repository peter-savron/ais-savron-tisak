package si.fri.ais.spec.core.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * A storage-produced comparison between two published revisions of one item.
 *
 * @param basePublishedAt publication time of version A
 * @param targetPublishedAt publication time of version B
 * @param edits non-overlapping edits in file order, empty for identical content
 * @param potentiallyStale whether the source was syncing when storage read the revisions
 */
@Builder
public record SpecDiff(@NotNull Instant basePublishedAt, @NotNull Instant targetPublishedAt,
                       @NotNull List<@Valid LineEdit> edits, boolean potentiallyStale) {
    public SpecDiff {
        edits = List.copyOf(edits);
    }

    /**
     * A contiguous line edit. An empty base list denotes insertion, an empty target list
     * denotes deletion, and two populated lists denote replacement.
     *
     * @param baseOffset number of lines preceding the edit in the base revision
     * @param targetOffset number of lines preceding the edit in the target revision
     * @param baseLines removed or replaced lines, with their original line endings if present
     * @param targetLines inserted or replacement lines, with their original line endings if present
     */
    @Builder
    public record LineEdit(@PositiveOrZero int baseOffset, @PositiveOrZero int targetOffset,
                           @NotNull List<String> baseLines, @NotNull List<String> targetLines) {
        public LineEdit {
            baseLines = List.copyOf(baseLines);
            targetLines = List.copyOf(targetLines);
        }
    }
}
