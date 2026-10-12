package io.github.khivus.torrserveflow.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uPlaylistTest {
    private val base = "http://localhost:8090/playlistall/all.m3u"

    @Test
    fun readsTitlesAndPreservesStreamParameters() {
        val url = "http://localhost:8090/stream/episode.mkv?link=abc&index=2&play"
        val entries = M3uPlaylist.parse("#EXTM3U\n#EXTINF:-1,Show, episode 2\n$url", base)
        assertEquals(listOf(PlaylistEntry("Show, episode 2", url)), entries)
    }

    @Test
    fun resolvesRelativeAndRootPaths() {
        val entries = M3uPlaylist.parse("../stream/first.mp4\n/stream/second.mp4?index=3", base)
        assertEquals("http://localhost:8090/stream/first.mp4", entries[0].url)
        assertEquals("http://localhost:8090/stream/second.mp4?index=3", entries[1].url)
    }

    @Test
    fun toleratesBomWindowsNewlinesAndComments() {
        val text = "\uFEFF#EXTM3U\r\n#EXTINF:0,Эпизод 1\r\n\r\n# a comment\r\n/stream/1.mkv\r\n"
        assertEquals("Эпизод 1", M3uPlaylist.parse(text, base).single().title)
    }

    @Test
    fun fallsBackToDecodedFilenameWithoutMetadata() {
        val entries = M3uPlaylist.parse("#EXTINF:0,\n/stream/My%20movie.mp4", base)
        assertEquals("My movie.mp4", entries.single().title)
    }

    @Test
    fun skipsMalformedAndUnsupportedUrlsWithoutReusingTheirTitle() {
        val text = "#EXTINF:0,Skipped\nfile:///private/file\n#EXTINF:0,Bad\nhttp://[invalid\n/stream/ok.mp4"
        assertEquals(listOf(PlaylistEntry("ok.mp4", "http://localhost:8090/stream/ok.mp4")), M3uPlaylist.parse(text, base))
    }

    @Test
    fun emptyPlaylistHasNoEntries() {
        assertTrue(M3uPlaylist.parse("#EXTM3U\n\n", base).isEmpty())
    }
}
