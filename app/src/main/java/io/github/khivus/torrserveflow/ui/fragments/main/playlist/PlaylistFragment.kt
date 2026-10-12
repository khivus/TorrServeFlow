package io.github.khivus.torrserveflow.ui.fragments.main.playlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.khivus.torrserveflow.R
import io.github.khivus.torrserveflow.app.App
import io.github.khivus.torrserveflow.ui.fragments.TSFragment
import io.github.khivus.torrserveflow.utils.M3uPlaylist
import io.github.khivus.torrserveflow.utils.Net
import io.github.khivus.torrserveflow.utils.PlaylistEntry

class PlaylistFragment : TSFragment() {
    private var entries = emptyList<PlaylistEntry>()
    private var playlistUrl = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.playlist_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnPlayAll).setOnClickListener { openPlayer(playlistUrl) }
        view.findViewById<View>(R.id.btnRetry).setOnClickListener { loadPlaylist() }
        view.findViewById<ListView>(R.id.lvPlaylist).setOnItemClickListener { _, _, position, _ ->
            entries.getOrNull(position)?.let { openPlayer(it.url) }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPlaylist()
    }

    private fun loadPlaylist() {
        val screen = view ?: return
        val state = screen.findViewById<View>(R.id.playlistState)
        val message = screen.findViewById<TextView>(R.id.playlistMessage)
        val retry = screen.findViewById<View>(R.id.btnRetry)
        val playAll = screen.findViewById<View>(R.id.btnPlayAll)
        val list = screen.findViewById<ListView>(R.id.lvPlaylist)
        // Ignore repeated retry clicks while the current request is running.
        if (retry.isEnabled.not()) return
        retry.isEnabled = false
        retry.visibility = View.GONE
        playAll.isEnabled = false
        list.visibility = View.GONE
        state.visibility = View.VISIBLE
        message.setText(R.string.playlist_loading)
        playlistUrl = Net.getHostUrl("/playlistall/all.m3u")
        val requestUrl = playlistUrl
        viewLifecycleOwner.lifecycleScope.launch {
            showProgress()
            try {
                entries = withContext(Dispatchers.IO) {
                    M3uPlaylist.parse(Net.getAuth(requestUrl), requestUrl)
                }
                list.adapter = ArrayAdapter(
                    requireContext(), R.layout.playlist_item, R.id.playlistTitle, entries.map { it.title }
                )
                state.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
                list.visibility = if (entries.isEmpty()) View.GONE else View.VISIBLE
                message.setText(R.string.playlist_empty)
                playAll.isEnabled = entries.isNotEmpty()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                entries = emptyList()
                message.setText(R.string.playlist_error)
                retry.visibility = View.VISIBLE
            } finally {
                retry.isEnabled = true
                hideProgress()
            }
        }
    }

    private fun openPlayer(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(url.toUri(), "video/*"))
        } catch (_: Exception) {
            App.toast(R.string.error_process_intent)
        }
    }
}
