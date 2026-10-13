package io.github.khivus.torrserveflow.utils

import com.google.gson.Gson
import io.github.khivus.torrserveflow.server.models.torrent.TorrentCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TorrentPlaybackStatusTest {
    @Test fun cachedDataWithNoReaderIsWaitingEvenWhenDownloadSpeedIsZero() {
        assertEquals(TorrentPlaybackStatus.CACHE_WAITING, status(bytes = 128L * 1024 * 1024, readers = 0))
    }

    @Test fun readerMeansConnectedStreamEvenWhenNoNetworkDownloadIsNeeded() {
        assertEquals(TorrentPlaybackStatus.STREAM_CONNECTED, status(bytes = 1024, readers = 1))
    }

    @Test fun missingCacheEndpointDoesNotAssertThatThePlayerDisconnected() {
        assertEquals(TorrentPlaybackStatus.CACHE_AVAILABLE, status(bytes = 1024, readers = null))
    }

    @Test fun closedTorrentDoesNotPromiseThatOldReportedBytesAreReusable() {
        assertEquals(TorrentPlaybackStatus.INACTIVE, status(state = 4, bytes = 1024, readers = 0))
        assertEquals(TorrentPlaybackStatus.INACTIVE, status(state = 5, bytes = 1024, readers = 0))
    }

    @Test fun startingBufferTakesPrecedenceOverItsTemporaryReader() {
        assertEquals(TorrentPlaybackStatus.BUFFERING, status(state = 2, bytes = 1024, readers = 1))
    }

    @Test fun emptyCacheDistinguishesDownloadFromIdle() {
        assertEquals(TorrentPlaybackStatus.DOWNLOADING, status(speed = 1000.0))
        assertEquals(TorrentPlaybackStatus.NO_BUFFER, status())
    }

    @Test fun metadataAndUnknownStatesStayDistinct() {
        assertEquals(TorrentPlaybackStatus.ADDED, status(state = 0))
        assertEquals(TorrentPlaybackStatus.GETTING_INFO, status(state = 1))
        assertEquals(TorrentPlaybackStatus.UNKNOWN, status(state = 99))
    }

    @Test fun cacheProtocolParsesCapacityBytesAndReaderCount() {
        val cache = Gson().fromJson("""{"Hash":"abc","Capacity":268435456,"Filled":134217728,"Readers":[{"Reader":12}]}""", TorrentCache::class.java)
        assertEquals(268435456L, cache.capacity)
        assertEquals(134217728L, cache.filled)
        assertEquals(1, cache.readers?.size)
    }

    @Test fun emptyOrPartialCacheReportPreservesUnknownReaders() {
        val empty = Gson().fromJson("{}", TorrentCache::class.java)
        assertNull(empty.hash)
        assertNull(empty.readers)
        val idle = Gson().fromJson("""{"Hash":"abc","Readers":[]}""", TorrentCache::class.java)
        assertEquals(0, idle.readers?.size)
    }

    private fun status(state: Int = 3, bytes: Long = 0, readers: Int? = 0, speed: Double = 0.0) =
        TorrentPlaybackStatus.resolve(state, bytes, readers, speed)
}
