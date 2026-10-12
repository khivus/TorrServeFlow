package io.github.khivus.torrserveflow.utils

import java.net.URI

data class PlaylistEntry(val title: String, val url: String)

object M3uPlaylist {
    fun parse(text: String, playlistUrl: String): List<PlaylistEntry> {
        val base = URI(playlistUrl)
        val entries = mutableListOf<PlaylistEntry>()
        var title: String? = null
        text.removePrefix("\uFEFF").lineSequence().forEach { raw ->
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF:", ignoreCase = true) -> {
                    title = line.substringAfter(',', "").trim().ifEmpty { null }
                }
                line.isEmpty() || line.startsWith('#') -> Unit
                else -> {
                    // Servers can emit either absolute URLs or paths relative to the playlist.
                    val uri = try {
                        base.resolve(line)
                    } catch (_: IllegalArgumentException) {
                        title = null
                        return@forEach
                    }
                    if (uri.scheme.equals("http", true) || uri.scheme.equals("https", true)) {
                        val name = uri.path?.substringAfterLast('/')?.ifEmpty { null }
                        entries += PlaylistEntry(title ?: name ?: line, uri.toString())
                    }
                    title = null
                }
            }
        }
        return entries
    }
}
