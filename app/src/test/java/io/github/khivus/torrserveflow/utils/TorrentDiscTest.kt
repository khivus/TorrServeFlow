package io.github.khivus.torrserveflow.utils

import com.google.gson.Gson
import io.github.khivus.torrserveflow.server.models.torrent.FileStat
import io.github.khivus.torrserveflow.server.models.torrent.Torrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TorrentDiscTest {
    @Test fun findsOnlyTheSelectedDiscInFileIdOrder() {
        val files = listOf(
            FileStat(9, "Movie/Disc 1/BDMV/STREAM/00001.m2ts", 1000),
            FileStat(7, "Movie/Disc 1/BDMV/PLAYLIST/00001.mpls", 100),
            FileStat(8, "Movie/Disc 1/BDMV/CLIPINF/00001.clpi", 100),
            FileStat(6, "Movie/Disc 1/BDMV/index.bdmv", 100),
            FileStat(10, "Movie/Disc 2/BDMV/PLAYLIST/00001.mpls", 100),
            FileStat(11, "Movie/extra.mkv", 1000)
        )
        val torrent = torrent(files)

        assertEquals(listOf(7, 8, 9), TorrentHelper.findDisc(torrent, 6)?.map { it.id })
        assertEquals(listOf(7, 8, 9), TorrentHelper.findDisc(torrent, 9)?.map { it.id })
        assertNull(TorrentHelper.findDisc(torrent, 11))
    }

    @Test fun doesNotTreatAnOrdinaryVideoOrIncompleteDiscAsADisc() {
        val torrent = torrent(listOf(FileStat(1, "Movie/BDMV/STREAM/00001.m2ts", 1000)))

        assertNull(TorrentHelper.findDisc(torrent, 1))
        assertNull(TorrentHelper.findDisc(torrent, 99))
    }

    @Test fun recognizesAvchdPlaylistsAndSubtitleExtensions() {
        assertTrue(TorrentHelper.isDiscPlaylist("BDMV/PLAYLIST/00001.MPL"))
        assertTrue(TorrentHelper.isDiscPlaylist("BDMV/PLAYLIST/00001.mpls"))
        assertFalse(TorrentHelper.isDiscPlaylist("BDMV/STREAM/00001.m2ts"))
        assertTrue(TorrentHelper.isSubtitles("Movie.ENG.SRT"))
        assertFalse(TorrentHelper.isSubtitles("Movie.mkv"))
    }

    private fun torrent(files: List<FileStat>): Torrent =
        Gson().fromJson("{}", Torrent::class.java).apply { file_stats = files }
}
