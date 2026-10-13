package io.github.khivus.torrserveflow.ui.fragments.play

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.khivus.torrserveflow.R
import io.github.khivus.torrserveflow.ext.append
import io.github.khivus.torrserveflow.server.local.TorrService
import io.github.khivus.torrserveflow.server.models.torrent.FileStat
import io.github.khivus.torrserveflow.settings.Settings
import io.github.khivus.torrserveflow.ui.activities.play.PlayActivity
import io.github.khivus.torrserveflow.ui.activities.play.PlaybackAction
import io.github.khivus.torrserveflow.ui.fragments.TSFragment
import io.github.khivus.torrserveflow.ui.fragments.play.viewmodels.InfoTorrent
import io.github.khivus.torrserveflow.ui.fragments.play.viewmodels.InfoViewModel
import io.github.khivus.torrserveflow.utils.CImageSpan
import io.github.khivus.torrserveflow.utils.Format
import io.github.khivus.torrserveflow.utils.SpanFormat
import io.github.khivus.torrserveflow.utils.ThemeUtil
import io.github.khivus.torrserveflow.utils.ThemeUtil.Companion.getColorFromAttr
import io.github.khivus.torrserveflow.utils.TorrentHelper
import io.github.khivus.torrserveflow.utils.TorrentPlaybackStatus
import java.io.File

open class InfoFragment : TSFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vi = inflater.inflate(R.layout.info_fragment, container, false)
        lifecycleScope.launch {
            (activity as? PlayActivity)?.showProgress()
            vi.findViewById<TextView?>(R.id.tvTitle)?.setText(R.string.loading_torrent)
            vi.findViewById<ConstraintLayout?>(R.id.clInfo)?.visibility = View.GONE
        }
        TorrService.start()
        return vi
    }

    private var posterUrl = " "
    private var latestInfo = InfoTorrent(null)
    private var renderedInfo: InfoTorrent? = null
    private var renderedIndex = -1
    private var renderedAction: PlaybackAction? = null
    private val infoViewModel: InfoViewModel
        get() = ViewModelProvider(this)[InfoViewModel::class.java]

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        infoViewModel.data.observe(viewLifecycleOwner) {
            latestInfo = it
            updateUI(it, (activity as? PlayActivity)?.torrentFileIndex ?: -1)
        }
        (activity as? PlayActivity)?.playbackAction?.observe(viewLifecycleOwner) {
            updateUI(latestInfo, (activity as? PlayActivity)?.torrentFileIndex ?: -1)
        }
    }

    override fun onStart() {
        super.onStart()
        infoViewModel.startUpdates()
    }

    override fun onStop() {
        infoViewModel.stopUpdates()
        super.onStop()
    }

    override fun onDestroyView() {
        renderedInfo = null
        super.onDestroyView()
    }

    suspend fun startInfo(hash: String) = withContext(Dispatchers.Main) {
        infoViewModel.setTorrent(hash)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(info: InfoTorrent, index: Int) {
        if (view == null) return
        val action = (activity as? PlayActivity)?.playbackAction?.value
        if (info == renderedInfo && index == renderedIndex && action == renderedAction) return
        renderedInfo = info
        renderedIndex = index
        renderedAction = action
        lifecycleScope.launch {
            view?.findViewById<ConstraintLayout?>(R.id.clInfo)?.visibility = View.VISIBLE
            renderStatus(info)
            if (info.torrent == null) {
                listOf(R.id.tvPeers, R.id.tvSeeds, R.id.tvSpeed).forEach {
                    view?.findViewById<TextView>(it)?.text = ""
                }
                listOf(R.id.tvDuration, R.id.tvBitrate, R.id.tvFileSize).forEach {
                    view?.findViewById<View>(it)?.visibility = View.GONE
                }
                return@launch
            }
            info.torrent?.let { torr ->
                view?.apply {
                    if (posterUrl != torr.poster) {
                        posterUrl = torr.poster ?: ""
                        if (posterUrl.isNotEmpty() && Settings.showCover())
                            findViewById<ImageView?>(R.id.ivPoster)?.let {
                                it.visibility = View.VISIBLE
                                Glide.with(this)
                                    .asBitmap()
                                    .load(posterUrl)
                                    .centerCrop()
                                    //.placeholder(ColorDrawable(0x3c000000))
                                    .apply(RequestOptions.bitmapTransform(RoundedCorners(6)))
                                    .transition(BitmapTransitionOptions.withCrossFade())
                                    .into(it)
                            }
                        else
                            findViewById<ImageView>(R.id.ivPoster)?.visibility = View.GONE
                    }
                    val category = torr.category ?: ""
                    val title = torr.title
                    val tv = findViewById<TextView>(R.id.tvTitle)

                    if (title.isEmpty() || (activity as? PlayActivity)?.let { it.isBrowsing || it.isShowingFiles } == true)
                        tv.visibility = View.GONE
                    else {
                        (activity as? PlayActivity)?.hideTitle()
                        tv.visibility = View.VISIBLE
                        //tv.text = title
                        if (category.isNotBlank()) {
                            val sIcon = SpannableString(" ")
                            val cDrawable: Drawable? = when {
                                category.contains("movie", true) -> AppCompatResources.getDrawable(requireContext(), R.drawable.round_movie_24)
                                category.contains("tv", true) -> AppCompatResources.getDrawable(requireContext(), R.drawable.round_live_tv_24)
                                category.contains("music", true) -> AppCompatResources.getDrawable(requireContext(), R.drawable.round_music_note_24)
                                category.contains("other", true) -> AppCompatResources.getDrawable(requireContext(), R.drawable.round_more_horiz_24)
                                else -> null
                            }
                            if (cDrawable == null)
                                tv.text = "${category.uppercase()} ● $title"
                            else {
                                cDrawable.setBounds(0, 0, cDrawable.intrinsicWidth, cDrawable.intrinsicHeight)
                                val span = CImageSpan(cDrawable)
                                sIcon.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                tv.text = SpanFormat.format("%s $title", sIcon)
                            }
                        } else
                            tv.text = title
                    }

                    val file: FileStat? = (activity as? PlayActivity)?.torrentFileId?.takeIf { it > 0 }
                        ?.let { TorrentHelper.findFile(torr, it) }
                        ?: TorrentHelper.getPlayableFiles(torr).getOrNull(index)
                    val themedContext = ContextThemeWrapper(this.context, ThemeUtil.selectedTheme)
                    val color1 = ColorUtils.setAlphaComponent(getColorFromAttr(themedContext, R.attr.colorBright), 200)
                    val color2 = ColorUtils.setAlphaComponent(getColorFromAttr(themedContext, R.attr.colorBright), 240)
                    val tvFileName = findViewById<TextView>(R.id.tvFileName)
                    val tvFileSize = findViewById<TextView>(R.id.tvFileSize)
                    file?.let {
                        var name = it.path
                        if (name.isNotEmpty())
                            name = File(name).name

                        tvFileName.visibility = View.VISIBLE
                        tvFileSize.visibility = View.VISIBLE

                        tvFileName.apply {
                            text = "" // name
                            append(name, color2)
                        }

                        val size = it.length
                        if (size >= 0) {
                            // spannable
                            tvFileSize.apply {
                                text = "" // txt
                                append("${getString(R.string.size)} ", color1, true)
                                append(Format.byteFmt(size), color2, true)
                            }
                        }
                    } ?: let {
                        tvFileName.visibility = View.GONE
                        tvFileSize.visibility = View.GONE
                    }

                    var prc = 0.0
                    if (torr.preload_size > 0)
                        prc = (torr.preloaded_bytes.toDouble() * 100.0 / torr.preload_size).coerceIn(0.0, 100.0)

                    if (torr.stat < TorrentHelper.T_STATE_WORKING) {
                        if (prc > 0 && prc < 100)
                            (activity as? PlayActivity)?.showProgress(prc.toInt())
                        else
                            (activity as? PlayActivity)?.showProgress()
                    } else
                        (activity as? PlayActivity)?.hideProgress()

                    val peers = "${torr.active_peers}/${torr.total_peers}"
                    if (peers.isNotEmpty()) {
                        // spannable
                        findViewById<TextView>(R.id.tvPeers).apply {
                            text = "" // txt
                            append("${getString(R.string.peers)} ", color1, true)
                            append(peers, color2, true)
                        }
                    }

                    val seeds = "${torr.connected_seeders}"
                    if (seeds.isNotEmpty()) {
                        // spannable
                        findViewById<TextView>(R.id.tvSeeds).apply {
                            text = "" // txt
                            append("${getString(R.string.seeds)} ", color1, true)
                            append(seeds, color2, true)
                        }
                    }

                    //val speed = Format.byteFmt(torr.download_speed) + getString(R.string.fmt_s)
                    val speed = Format.speedFmt(torr.download_speed)
                    if (speed.isNotEmpty()) {
                        // spannable
                        findViewById<TextView>(R.id.tvSpeed).apply {
                            text = "" // txt
                            append("${getString(R.string.download_speed)} ", color1, true)
                            append(speed, color2, true)
                        }
                    }
                    // ffprobe addons
                    val tvdr = findViewById<TextView>(R.id.tvDuration)
                    torr.duration_seconds?.takeIf { it.isFinite() && it > 0 }?.let { ds ->
                        val duration = Format.durFmtS(ds)
                        tvdr.apply {
                            text = "" // txt
                            append("${getString(R.string.runtime)} ", color1, true)
                            append(duration, color2, true)
                            visibility = View.VISIBLE
                        }
                    } ?: let {
                        tvdr.visibility = View.GONE
                    }
                    val tvbr = findViewById<TextView>(R.id.tvBitrate)
                    torr.bit_rate?.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }?.let { br ->
                        val bitRate = Format.speedFmt(br / 8)
                        tvbr.apply {
                            text = "" // txt
                            append("${getString(R.string.bit_rate)} ", color1, true)
                            append(bitRate, color2, true)
                            visibility = View.VISIBLE
                        }
                    } ?: let {
                        tvbr.visibility = View.GONE
                    }

                }
            }
        }
    }

    private fun renderStatus(info: InfoTorrent) {
        val screen = view ?: return
        val torr = info.torrent
        val uri = Uri.parse(Settings.getHost())
        val address = uri.host.orEmpty() + if (uri.port > 0) ":${uri.port}" else ""
        val connection = when {
            torr != null || info.serverConnected == true -> R.string.playback_server_connected
            info.serverConnected == false -> R.string.playback_server_unreachable
            else -> R.string.playback_server_checking
        }
        screen.findViewById<TextView>(R.id.tvServerConnection).text = getString(
            if (TorrService.isLocal()) R.string.playback_local_server else R.string.playback_remote_server,
            getString(connection), address
        )
        val action = when ((activity as? PlayActivity)?.playbackAction?.value) {
            PlaybackAction.BROWSING_FILES -> R.string.playback_app_browsing
            PlaybackAction.PREPARING_PLAYBACK -> R.string.playback_app_preparing
            PlaybackAction.CHOOSING_PLAYER -> R.string.playback_app_choosing
            PlaybackAction.OPENING_PLAYER -> R.string.playback_app_opening
            PlaybackAction.PLAYER_OPENED -> R.string.playback_app_opened
            PlaybackAction.RETURNED_FROM_PLAYER -> R.string.playback_app_returned
            PlaybackAction.ERROR -> R.string.playback_app_error
            else -> R.string.playback_app_loading
        }
        screen.findViewById<TextView>(R.id.tvAppAction).setText(action)
        if (torr == null) {
            screen.findViewById<TextView>(R.id.tvInfo).setText(
                if (info.error.isBlank()) R.string.playback_status_checking else R.string.playback_status_unavailable
            )
            screen.findViewById<TextView>(R.id.tvBuffer).setText(R.string.playback_cache_unknown)
            screen.findViewById<TextView>(R.id.tvCacheHint).text = info.error.ifBlank { getString(R.string.playback_hint_checking) }
            return
        }
        val active = torr.stat == TorrentHelper.T_STATE_WORKING || torr.stat == TorrentHelper.T_STATE_PRELOAD
        val bytes = if (active) (info.cache?.filled ?: torr.preloaded_bytes).coerceAtLeast(0) else 0L
        val readers = info.cache?.readers?.size
        val status = TorrentPlaybackStatus.resolve(torr.stat, bytes, readers, torr.download_speed)
        val label = when (status) {
            TorrentPlaybackStatus.ADDED -> R.string.playback_status_added
            TorrentPlaybackStatus.GETTING_INFO -> R.string.playback_status_getting_info
            TorrentPlaybackStatus.BUFFERING -> R.string.playback_status_buffering
            TorrentPlaybackStatus.STREAM_CONNECTED -> R.string.playback_status_stream_connected
            TorrentPlaybackStatus.CACHE_WAITING -> R.string.playback_status_cache_waiting
            TorrentPlaybackStatus.CACHE_AVAILABLE -> R.string.playback_status_cache_available
            TorrentPlaybackStatus.DOWNLOADING -> R.string.playback_status_downloading
            TorrentPlaybackStatus.NO_BUFFER -> R.string.playback_status_no_buffer
            TorrentPlaybackStatus.INACTIVE -> R.string.playback_status_inactive
            TorrentPlaybackStatus.UNKNOWN -> R.string.playback_status_unknown
        }
        screen.findViewById<TextView>(R.id.tvInfo).setText(label)
        val capacity = info.cache?.capacity?.takeIf { it > 0 }?.let { Format.byteFmt(it) }
        val cacheText = if (capacity != null) getString(R.string.playback_cache_capacity, Format.byteFmt(bytes), capacity)
            else getString(R.string.playback_cache_bytes, Format.byteFmt(bytes))
        val streams = getString(R.string.playback_streams, readers?.toString() ?: getString(R.string.playback_not_reported))
        val preload = if (torr.stat == TorrentHelper.T_STATE_PRELOAD && torr.preload_size > 0)
            "\n" + getString(R.string.playback_preload_progress, Format.byteFmt(torr.preloaded_bytes.coerceAtLeast(0)), Format.byteFmt(torr.preload_size))
        else ""
        screen.findViewById<TextView>(R.id.tvBuffer).text = "$cacheText\n$streams$preload"
        val hint = when (status) {
            TorrentPlaybackStatus.CACHE_WAITING, TorrentPlaybackStatus.CACHE_AVAILABLE -> R.string.playback_hint_cached
            TorrentPlaybackStatus.STREAM_CONNECTED -> R.string.playback_hint_stream
            TorrentPlaybackStatus.BUFFERING, TorrentPlaybackStatus.DOWNLOADING -> R.string.playback_hint_buffering
            TorrentPlaybackStatus.NO_BUFFER, TorrentPlaybackStatus.INACTIVE -> R.string.playback_hint_no_buffer
            TorrentPlaybackStatus.ADDED, TorrentPlaybackStatus.GETTING_INFO -> R.string.playback_hint_metadata
            else -> R.string.playback_hint_unknown
        }
        screen.findViewById<TextView>(R.id.tvCacheHint).setText(hint)
    }
}
