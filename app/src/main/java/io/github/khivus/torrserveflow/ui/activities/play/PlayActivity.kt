package io.github.khivus.torrserveflow.ui.activities.play

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.khivus.torrserveflow.BuildConfig
import io.github.khivus.torrserveflow.R
import io.github.khivus.torrserveflow.ad.AD
import io.github.khivus.torrserveflow.app.App
import io.github.khivus.torrserveflow.server.api.Api
import io.github.khivus.torrserveflow.server.local.TorrService
import io.github.khivus.torrserveflow.server.models.torrent.Torrent
import io.github.khivus.torrserveflow.ui.activities.play.Play.play
import io.github.khivus.torrserveflow.ui.fragments.main.servfinder.ServerFinderFragment
import io.github.khivus.torrserveflow.ui.fragments.main.update.server.UpdaterServer
import io.github.khivus.torrserveflow.ui.fragments.play.ChooserFragment
import io.github.khivus.torrserveflow.ui.fragments.play.InfoFragment
import io.github.khivus.torrserveflow.utils.ThemeUtil
import kotlin.concurrent.thread


class PlayActivity : AppCompatActivity() {
    var torrentLink: String = ""
    var torrentHash: String = ""
    var torrentTitle: String = ""
    var torrentPoster: String = ""
    var torrentCategory: String = ""
    var torrentData: String = ""
    var torrentSave: Boolean = false
    var torrentFileIndex: Int = -1

    private var userClose = false
    private var intentProcessed = false
    val isBrowsing: Boolean
        get() = intent.getStringExtra("action") == "browse"
    var isShowingFiles: Boolean = false
        private set

    var ad: AD? = null

    private val themeUtil = ThemeUtil()

    val infoFragment = InfoFragment()


    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (BuildConfig.DEBUG) Log.d("PlayActivity", "handleOnBackPressed() set userClose = true")
            userClose = true
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeUtil.onCreate(this)
        setContentView(R.layout.play_activity)
        readArgs()
        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationContentDescription(R.string.navigate_back)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            title = if (isBrowsing) getString(R.string.torrent_files)
            else torrentTitle.ifBlank { getString(R.string.torrent_files) }
            inflateMenu(R.menu.torrent_details_menu)
            menu.findItem(R.id.actionTorrentDetails).isChecked = !isBrowsing
            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.actionTorrentDetails) {
                    item.isChecked = !item.isChecked
                    this@PlayActivity.findViewById<View>(R.id.info_container).visibility = if (item.isChecked) View.VISIBLE else View.GONE
                    true
                } else false
            }
        }
        if (isBrowsing) findViewById<View>(R.id.info_container).visibility = View.GONE

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        lifecycleScope.launch { showProgress() }


        if (intent == null) {
            error(ErrIntentNull)
            return
        }

        TorrService.start()
    }

    override fun onResume() {
        super.onResume()
        themeUtil.onResume(this)
        if (intentProcessed) return
        readArgs()
        lifecycleScope.launch(Dispatchers.IO) {
            if (!TorrService.wait(5)) {
                error(ErrTorrServerNotResponding)
                delay(App.LONG_TOAST_DURATION.toLong())
                if (TorrService.isLocal() && !TorrService.isInstalled()) {
                    withContext(Dispatchers.Main) {
                        findViewById<MaterialToolbar>(R.id.toolbar).setTitle(R.string.install_server)
                    }
                    UpdaterServer.updateFromNet {
                        lifecycleScope.launch(Dispatchers.Main) {
                            showProgress(it)
                        }
                    }
                    if (TorrService.wait(5))
                        withContext(Dispatchers.Main) {
                            findViewById<MaterialToolbar>(R.id.toolbar).setTitle(R.string.torrent_files)
                            processIntent()
                        }
                } else {
                    //server is not local, change
                    ServerFinderFragment().apply {
                        show(App.currentActivity as? FragmentActivity?, R.id.bottom_container, true)
                        onResult = {
                            withContext(Dispatchers.Main) {
                                processIntent()
                            }
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    processIntent()
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (BuildConfig.DEBUG) Log.d("PlayActivity", "onUserLeaveHint()")
        if (!isBrowsing) {
            lifecycleScope.cancel()
            finish()
        }
    }

    override fun onDestroy() {
        if (userClose) {
            if (torrentHash.isNotEmpty())
                thread {
                    try {
                        if (BuildConfig.DEBUG) Log.d("PlayActivity", "onDestroy() drop torrent $torrentHash")
                        Api.dropTorrent(torrentHash)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (App.inForeground) super.onConfigurationChanged(newConfig)
        themeUtil.onConfigurationChanged(this, newConfig)
    }

    private fun processIntent() {
        if (intentProcessed) return
        intentProcessed = true
        ad = AD(findViewById(R.id.ivAd), this)
        ad?.get()
        //// Play torrent
        if (isBrowsing || intent.getStringExtra("action") == "play")
            play(false)
        else {
            lifecycleScope.launch { hideProgress() }
            if (App.inForeground) {
                val isForceChoose = (intent.hasExtra("action") && intent.getStringExtra("action") == "choose")
                ChooserFragment().show(this, isForceChoose) {
                    when (it) {
                        1, 2 -> {
                            play(it == 2)
                        }

                        3 -> {
                            addAndExit()
                        }
                    }
                }
            }
        }
    }

    // https://material.io/components/progress-indicators/android
    suspend fun showProgress(prc: Int = -1) = withContext(Dispatchers.Main) {
        if (isActive) {
            val color = ThemeUtil.getColorFromAttr(this@PlayActivity, R.attr.colorAccent)
            findViewById<LinearProgressIndicator>(R.id.progressBar)?.apply {
                setIndicatorColor(color)
                visibility = View.VISIBLE
                isIndeterminate = prc < 0
                if (!isIndeterminate)
                    setProgressCompat(prc, true)
            }
        }
    }

    suspend fun hideProgress() = withContext(Dispatchers.Main) {
        if (isActive)
            findViewById<LinearProgressIndicator>(R.id.progressBar)?.apply {
                visibility = View.INVISIBLE
                isIndeterminate = true
            }
    }

    suspend fun hideTitle() = withContext(Dispatchers.Main) {
        if (isActive && !isBrowsing && !isShowingFiles && torrentTitle.isNotBlank()) {
            val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
            if (toolbar.title != torrentTitle) toolbar.title = torrentTitle
        }
    }

    suspend fun showTorrentTitle(torrent: Torrent) = withContext(Dispatchers.Main) {
        torrentTitle = torrent.title.ifBlank { torrent.name }
        findViewById<MaterialToolbar>(R.id.toolbar).title = if (isBrowsing || isShowingFiles) getString(R.string.torrent_files)
        else torrentTitle.ifBlank { getString(R.string.torrent_files) }
    }

    fun onFileListShown() {
        isShowingFiles = true
        findViewById<MaterialToolbar>(R.id.toolbar).setTitle(R.string.torrent_files)
    }

}
