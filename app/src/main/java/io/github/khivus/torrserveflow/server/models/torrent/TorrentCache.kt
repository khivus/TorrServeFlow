package io.github.khivus.torrserveflow.server.models.torrent

import com.google.gson.annotations.SerializedName

data class TorrentCache(
    @SerializedName("Hash") val hash: String? = null,
    @SerializedName("Capacity") val capacity: Long = 0,
    @SerializedName("Filled") val filled: Long = 0,
    @SerializedName("Readers") val readers: List<CacheReader>? = null
)

data class CacheReader(
    @SerializedName("Reader") val piece: Int = 0
)
