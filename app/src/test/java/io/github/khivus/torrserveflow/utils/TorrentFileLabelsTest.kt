package io.github.khivus.torrserveflow.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TorrentFileLabelsTest {
    @Test
    fun screenshotStyleReleaseShowsOnlyDistinctEpisodes() {
        val heading = "Daemons of the Shadow Realm 2026 S01 1080p CR WEB-DL DUAL DDP2.0 H.264-An0Zu"
        val paths = (1..9).map { episode ->
            "$heading/Daemons of the Shadow Realm 2026 S01E0$episode 1080p CR WEB-DL DUAL DDP2.0 H.264-An0Zu.mkv"
        }
        val labels = TorrentFileLabels.create(heading, paths)
        assertEquals((1..9).map { "S01E0$it" }, labels.map { it.title })
        assertTrue(labels.all { it.folder.isEmpty() })
    }

    @Test
    fun preservesEpisodeWordWhenRemainingDifferenceIsNumeric() {
        val labels = TorrentFileLabels.create("Show Episodes", listOf(
            "Show/Show Episodes 01.mkv", "Show/Show Episodes 02.mkv"
        ))
        assertEquals(listOf("Episodes 01", "Episodes 02"), labels.map { it.title })
    }

    @Test
    fun keepsContextThatIsNotShownInHeading() {
        val labels = TorrentFileLabels.create("Custom title", listOf(
            "Show/Show S01E01 1080p.mkv", "Show/Show S01E02 1080p.mkv"
        ))
        assertEquals(listOf("Show S01E01 1080p", "Show S01E02 1080p"), labels.map { it.title })
    }

    @Test
    fun distinctSubfoldersRemainVisibleForIdenticalBasenames() {
        val labels = TorrentFileLabels.create("Show", listOf(
            "Show/Season 1/Episode 01.mkv", "Show/Season 2/Episode 01.mkv"
        ))
        assertEquals(listOf("Season 1", "Season 2"), labels.map { it.folder })
        assertEquals(listOf("Episode 01", "Episode 01"), labels.map { it.title })
    }

    @Test
    fun doesNotStripPartialEpisodeTokens() {
        val labels = TorrentFileLabels.create("Show S01", listOf("Show/Show S01E01.mkv", "Show/Show S01E02.mkv"))
        assertEquals(listOf("S01E01", "S01E02"), labels.map { it.title })
    }

    @Test
    fun singleFileKeepsItsNameAndRemovesRepeatedParent() {
        assertEquals(listOf(TorrentFileLabel("My Movie", "")),
            TorrentFileLabels.create("My Movie", listOf("My Movie/My.Movie.mkv")))
    }

    @Test
    fun doesNotConcealPunctuationDifferences() {
        val labels = TorrentFileLabels.create("Show", listOf("Show/Show.Finale.mkv", "Show/Show_Finale.mkv"))
        assertEquals(listOf("Show.Finale", "Show_Finale"), labels.map { it.title })
    }

    @Test
    fun handlesWindowsSeparatorsAndEmptyLists() {
        val labels = TorrentFileLabels.create("Show", listOf("Show\\Season 1\\S01E01.mp4", "Show\\Season 2\\S02E01.mp4"))
        assertEquals(listOf("Season 1", "Season 2"), labels.map { it.folder })
        assertTrue(TorrentFileLabels.create("Show", emptyList()).isEmpty())
    }
}
