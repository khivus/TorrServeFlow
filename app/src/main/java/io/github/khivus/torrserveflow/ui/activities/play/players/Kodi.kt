package io.github.khivus.torrserveflow.ui.activities.play.players

import android.content.Intent
import androidx.core.net.toUri
import io.github.khivus.torrserveflow.ext.urlEncode
import io.github.khivus.torrserveflow.server.models.torrent.Torrent
import io.github.khivus.torrserveflow.utils.Mime
import io.github.khivus.torrserveflow.utils.Net
import io.github.khivus.torrserveflow.utils.TorrentHelper
import io.github.khivus.torrserveflow.settings.Settings

object Kodi {

    fun getIntent(pkg: String, torrent: Torrent, index: Int): Intent {

        val files = TorrentHelper.getPlayableFiles(torrent)

        return Intent(Intent.ACTION_VIEW).apply {

            setPackage(pkg)

            val hasSubtitles = torrent.file_stats.orEmpty().any {
                it.path.endsWith(".srt", ignoreCase = true) ||
                it.path.endsWith(".ass", ignoreCase = true)
            }

            if (Settings.kodiPlaylist() && (files.size > 1 || hasSubtitles)) {
                val playlist = Net.getHostUrl(
                    "/playlist/${torrent.name.urlEncode()}.m3u?hash=${torrent.hash}&index=$index"
                )

                setDataAndType(
                    playlist.toUri(),
                    "audio/x-mpegurl"
                )

            } else {
                val file = TorrentHelper.findFile(torrent, index)
                    ?: throw Exception("file in torrent not found")

                val link = TorrentHelper.getTorrentPlayLink(torrent, index)

                setDataAndType(
                    link.toUri(),
                    Mime.getMimeType(file.path)
                )
            }
        }
    }
}
