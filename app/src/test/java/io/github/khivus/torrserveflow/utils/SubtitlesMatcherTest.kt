package io.github.khivus.torrserveflow.utils

import io.github.khivus.torrserveflow.server.models.torrent.FileStat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubtitlesMatcherTest {
    @Test fun matchesEpisodeSubtitlesWithoutTakingTheNextEpisode() {
        val english = FileStat(3, "Show.S01E01.eng.srt", 100)
        val nextEpisode = FileStat(4, "Show.S01E010.eng.srt", 100)
        val (matched, remaining) = SubtitlesMatcher.findSubtitles("Show.S01E01", listOf(english, nextEpisode)) { "stream/$it" }

        assertEquals(listOf("stream/3"), matched.map { it.uri })
        assertEquals(listOf(nextEpisode), remaining)
    }

    @Test fun preservesLanguageLabelsAndSubtitleFormats() {
        val files = listOf(FileStat(3, "Movie.eng.forced.ASS", 100), FileStat(4, "Movie.rus.vtt", 100))
        val (matched, remaining) = SubtitlesMatcher.findSubtitles("Movie", files) { "stream/$it" }

        assertEquals(listOf("eng", "rus"), matched.map { it.language })
        assertEquals("forced", matched.first().label)
        assertEquals(listOf("text/x-ssa", "text/x-vtt"), matched.map { it.mimeType })
        assertEquals(emptyList<FileStat>(), remaining)
    }

    @Test fun acceptsUntaggedSubtitlesWithTheSameName() {
        val (matched, _) = SubtitlesMatcher.findSubtitles("Movie", listOf(FileStat(3, "Movie.srt", 100))) { "stream/$it" }

        assertEquals("application/x-subrip", matched.single().mimeType)
        assertNull(matched.single().language)
    }
}
