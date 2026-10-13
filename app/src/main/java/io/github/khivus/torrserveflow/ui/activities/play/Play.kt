package io.github.khivus.torrserveflow.ui.activities.play

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.khivus.torrserveflow.R
import io.github.khivus.torrserveflow.app.App
import io.github.khivus.torrserveflow.server.api.Api
import io.github.khivus.torrserveflow.server.models.torrent.Torrent
import io.github.khivus.torrserveflow.settings.Settings
import io.github.khivus.torrserveflow.ui.activities.play.players.Players
import io.github.khivus.torrserveflow.ui.dialogs.PlayersDialog
import io.github.khivus.torrserveflow.ui.fragments.play.TorrentFilesFragment
import io.github.khivus.torrserveflow.utils.TorrentHelper

object Play {

    var tmpPlayerPackage: String? = null

    fun PlayActivity.play(save: Boolean) {
        infoFragment.show(this, R.id.info_container)
        lifecycleScope.launch(Dispatchers.IO) {
            showProgress(-1)
            torrentSave = save

            val torrent: Torrent
            try {
                val torr = addTorrent(torrentHash, torrentLink, torrentTitle, torrentPoster, torrentCategory, torrentData, torrentSave)
                    ?: let {
                        error(ErrLoadTorrent)
                        return@launch
                    }
                infoFragment.startInfo(torr.hash)
                if (torrentHash.isEmpty() && torr.hash.isNotBlank()) // store hash for Api.dropTorrent on close
                    torrentHash = torr.hash
                torrent = TorrentHelper.waitFiles(torr.hash) ?: let {
                    error(ErrLoadTorrentInfo)
                    return@launch
                }
            } catch (e: Exception) {
                e.message?.let { App.toast(it, true) }
                delay(App.LONG_TOAST_DURATION.toLong())
                return@launch
            }

            val viewed = try {
                Api.listViewed(torrent.hash)
            } catch (_: Exception) {
                emptyList()
            }
            val files = TorrentHelper.getPlayableFiles(torrent)
            showTorrentTitle(torrent)

            if (intent.hasExtra("FileTemplate") && torrentFileIndex == -1) // For lostfilm app
                torrentFileIndex = SerialFilter.filter(intent, files)

            lifecycleScope.launch {
                when {
                    files.isEmpty() -> {
                        error(ErrLoadTorrentFiles)
                    }

                    files.size == 1 && !isBrowsing -> {
                        torrentFileIndex = 0
                        lifecycleScope.launch {
                            streamTorrent(torrent, files.first().id)
                        }
                    }

                    torrentFileIndex > 0 && !isBrowsing -> {
                        lifecycleScope.launch {
                            streamTorrent(torrent, torrentFileIndex)
                        }
                    }

                    else -> {
                        hideProgress()
                        TorrentFilesFragment().showTorrent(this@play, torrent, viewed) { file ->
                            torrentFileIndex = TorrentHelper.findIndex(torrent, file)
                            lifecycleScope.launch {
                                streamTorrent(torrent, file.id)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun PlayActivity.streamTorrent(torrent: Torrent, index: Int) {
        onFileSelected(index)
        showProgress()
        var torr = torrent

        TorrentHelper.preloadTorrent(torr, index)
        delay(200)

        withContext(Dispatchers.IO) {
            try {
                torr = Api.getTorrent(torr.hash)
            } catch (_: Exception) {
            }
            while (torr.stat == TorrentHelper.T_STATE_PRELOAD) {
                delay(1000)
                try {
                    torr = Api.getTorrent(torr.hash)
                } catch (_: Exception) {
                }
            }
        }

        ad?.waitAd()
        var playerIntent: Intent? = null
        try {
            if (tmpPlayerPackage != null) {
                playerIntent = Players.getIntent(torr, index, tmpPlayerPackage)
                tmpPlayerPackage = null
            } else if (Settings.getPlayer().isEmpty()) {
                // show chooser
                playbackAction.value = PlaybackAction.CHOOSING_PLAYER
                PlayersDialog.show(this, torr, index) { player ->
                    playerIntent = Players.getIntent(torr, index, player)
                    playerIntent?.let { openPlayer(it) }
                }
            } else
                playerIntent = Players.getIntent(torr, index, null)
        } catch (e: Exception) {
            playbackAction.value = PlaybackAction.ERROR
            e.message?.let { App.toast(it) }
        }
        playerIntent?.let { openPlayer(it) }
    }
}
