package io.github.khivus.torrserveflow.utils

enum class TorrentPlaybackStatus {
    ADDED, GETTING_INFO, BUFFERING, STREAM_CONNECTED, CACHE_WAITING,
    CACHE_AVAILABLE, DOWNLOADING, NO_BUFFER, INACTIVE, UNKNOWN;

    companion object {
        fun resolve(state: Int, cachedBytes: Long, readers: Int?, downloadSpeed: Double): TorrentPlaybackStatus =
            when (state) {
                TorrentHelper.T_STATE_ADDED -> ADDED
                TorrentHelper.T_STATE_GETINFO -> GETTING_INFO
                TorrentHelper.T_STATE_PRELOAD -> BUFFERING
                TorrentHelper.T_STATE_WORKING -> when {
                    readers != null && readers > 0 -> STREAM_CONNECTED
                    cachedBytes > 0 && readers == 0 -> CACHE_WAITING
                    cachedBytes > 0 -> CACHE_AVAILABLE
                    downloadSpeed > 0 -> DOWNLOADING
                    else -> NO_BUFFER
                }
                TorrentHelper.T_STATE_CLOSED, TorrentHelper.T_STATE_INDB -> INACTIVE
                else -> UNKNOWN
            }
    }
}
