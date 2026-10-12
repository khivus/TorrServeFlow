package io.github.khivus.torrserveflow.atv.channels.providers

import io.github.khivus.torrserveflow.server.models.torrent.Torrent


open class VideoProvider {
    open fun get(): List<Torrent> = emptyList()
}

object Provider {
    const val Torrents = "torrents"

    private val providers = mapOf<String, VideoProvider>(
        Torrents to Torrents()
    )

    fun get(name: String): List<Torrent> {
        try {
            return providers[name]?.get() ?: emptyList()
        } catch (e: Exception) {
            return emptyList()
        }
    }
}
