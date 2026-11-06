package app.myspoti.host.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CarLyricsTest {
    private val lines = listOf(
        CarLyricLine(1_000, "Twinkle, twinkle, little star"),
        CarLyricLine(4_000, "How I wonder what you are"),
        CarLyricLine(7_000, ""),
        CarLyricLine(10_000, "Up above the world so high"),
    )

    @Test fun lineAtPosition() {
        assertEquals(-1, CarLyrics.indexAt(lines, 0))
        assertEquals(0, CarLyrics.indexAt(lines, 1_000))
        assertEquals(0, CarLyrics.indexAt(lines, 3_999))
        assertEquals(1, CarLyrics.indexAt(lines, 4_000))
        assertEquals(3, CarLyrics.indexAt(lines, 99_000))
        assertEquals(-1, CarLyrics.indexAt(emptyList(), 5_000))
    }

    @Test fun timeToNextLine() {
        assertEquals(1_000L, CarLyrics.untilNext(lines, -1, 0))
        assertEquals(500L, CarLyrics.untilNext(lines, 0, 3_500))
        assertNull(CarLyrics.untilNext(lines, 3, 12_000))
    }

    @Test fun introShowsTheFirstLineNext() {
        assertEquals(CarLyrics.GAP to "Twinkle, twinkle, little star", CarLyrics.texts(lines, -1))
    }

    @Test fun currentAndNextLine() {
        assertEquals("Twinkle, twinkle, little star" to "How I wonder what you are", CarLyrics.texts(lines, 0))
    }

    @Test fun gapsShowANote() {
        assertEquals("How I wonder what you are" to CarLyrics.GAP, CarLyrics.texts(lines, 1))
        assertEquals(CarLyrics.GAP to "Up above the world so high", CarLyrics.texts(lines, 2))
    }

    @Test fun lastLineHasNoNext() {
        assertEquals("Up above the world so high" to "", CarLyrics.texts(lines, 3))
    }
}
