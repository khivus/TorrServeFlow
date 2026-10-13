package io.github.khivus.torrserveflow.ui.fragments.play.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.khivus.torrserveflow.R
import io.github.khivus.torrserveflow.app.App
import io.github.khivus.torrserveflow.server.api.Api
import io.github.khivus.torrserveflow.server.models.torrent.Torrent
import io.github.khivus.torrserveflow.server.models.torrent.TorrentCache

data class InfoTorrent(
    val torrent: Torrent?, val error: String = "", val cache: TorrentCache? = null,
    val serverConnected: Boolean? = null
)

class InfoViewModel : ViewModel() {
    private val mutableData = MutableLiveData(InfoTorrent(null))
    val data: LiveData<InfoTorrent> = mutableData
    private var hash = ""
    private var updateJob: Job? = null

    fun setTorrent(hash: String) {
        if (this.hash == hash) return
        this.hash = hash
        stopUpdates()
        startUpdates()
    }

    fun startUpdates() {
        if (hash.isBlank() || updateJob?.isActive == true) return
        // Recheck on return from a player instead of showing a potentially expired cache sample.
        mutableData.value = InfoTorrent(null)
        val torrentHash = hash
        updateJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val info = try {
                    val torrent = Api.getTorrent(torrentHash)
                    val cache = try {
                        Api.getTorrentCache(torrentHash)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        null // Older servers may not expose detailed cache statistics.
                    }
                    InfoTorrent(torrent, cache = cache)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    InfoTorrent(null, e.message ?: App.context.getString(R.string.error_retrieve_torrent_info),
                        serverConnected = Api.echo().isNotBlank())
                }
                withContext(Dispatchers.Main) { mutableData.value = info }
                delay(1000)
            }
        }
    }

    fun stopUpdates() {
        updateJob?.cancel()
        updateJob = null
    }
}
