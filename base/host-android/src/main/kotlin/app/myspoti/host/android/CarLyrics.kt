package app.myspoti.host.android

/** A synced lyric line for the car ([PlaybackService.updateCarLyrics]): [text] blank for an instrumental gap. */
data class CarLyricLine(val timeMs: Long, val text: String)

/**
 * The lyrics Android Auto shows in place of the song: the line sung now as the title, the next one
 * as the subtitle. The car draws its own player and only reads those fields, so a line is all it
 * can show, not the app's word-by-word view.
 */
object CarLyrics {
    /** No line shown: the car shows the song's own title and artist. */
    const val OFF = Int.MIN_VALUE

    /** A line goes up this much early: the car takes a moment to redraw. */
    const val LEAD_MS = 300L

    /** What stands for a gap (the intro, an instrumental part). */
    const val GAP = "♪"

    /** The line sung at [positionMs] in [lines] (sorted by time); -1 before the first one. */
    fun indexAt(lines: List<CarLyricLine>, positionMs: Long): Int = lines.indexOfLast { it.timeMs <= positionMs }

    /** The time from [positionMs] to the line after [index]; null after the last one. */
    fun untilNext(lines: List<CarLyricLine>, index: Int, positionMs: Long): Long? =
        lines.getOrNull(index + 1)?.let { it.timeMs - positionMs }

    /** The title and the subtitle the car shows at line [index] (-1: the intro). */
    fun texts(lines: List<CarLyricLine>, index: Int): Pair<String, String> {
        val now = lines.getOrNull(index)?.text?.trim().orEmpty().ifBlank { GAP }
        val next = lines.getOrNull(index + 1)?.text?.trim()?.ifBlank { GAP }.orEmpty()
        return now to next
    }
}
