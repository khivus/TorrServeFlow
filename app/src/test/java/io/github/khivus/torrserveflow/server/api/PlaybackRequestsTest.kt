package io.github.khivus.torrserveflow.server.api

import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackRequestsTest {
    @Test fun playbackPositionUsesSecondsAndTheServerFileId() {
        val request = JsonParser.parseString(ViewedReq("set", "torrent", 17, 123.75).toString()).asJsonObject

        assertEquals("set", request["action"].asString)
        assertEquals("torrent", request["hash"].asString)
        assertEquals(17, request["file_index"].asInt)
        assertEquals(123.75, request["timecode"].asDouble, 0.0)
    }

    @Test fun legacyViewedEntriesHaveNoResumePosition() {
        val viewed = Gson().fromJson("""{"hash":"torrent","file_index":17}""", Viewed::class.java)

        assertEquals(17, viewed.file_index)
        assertEquals(0.0, viewed.timecode, 0.0)
    }

    @Test fun cacheRequestsRemainAvailableAlongsideResumeRequests() {
        val request = JsonParser.parseString(CacheReq("torrent").toString()).asJsonObject

        assertEquals("get", request["action"].asString)
        assertEquals("torrent", request["hash"].asString)
    }
}
