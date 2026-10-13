package io.github.khivus.torrserveflow.ui.activities.play.players

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.net.toUri
import io.github.khivus.torrserveflow.R
import io.github.khivus.torrserveflow.app.App
import io.github.khivus.torrserveflow.app.Consts.PLAYERS_BLACKLIST
import io.github.khivus.torrserveflow.server.models.torrent.Torrent
import io.github.khivus.torrserveflow.settings.Settings
import io.github.khivus.torrserveflow.utils.Mime
import io.github.khivus.torrserveflow.utils.TorrentHelper
import java.io.File
import java.util.Locale

object Players {

    fun getIntent(torrent: Torrent, index: Int, selectedPlayer: String?): Intent? {
        val file = TorrentHelper.findFile(torrent, index) ?: throw Exception("file in torrent not found")
        val link = TorrentHelper.getTorrentPlayLink(torrent, index)
        val player = selectedPlayer ?: Settings.getPlayer()
        val mime = Mime.getMimeType(file.path)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(link.toUri(), mime)
        intent.putExtra("title", torrent.title)
        intent.putExtra("poster", torrent.poster)
        // default os player
        if (player == "0" && intent.resolveActivity(App.context.packageManager) != null)
            return intent
        // ViMu player
        if (player == "net.gtvbox.videoplayer" || player == "net.gtvbox.vimuhd") {
            val vimuIntent = Vimu.getIntent(player, torrent, index)
            if (vimuIntent.resolveActivity(App.context.packageManager) != null)
                return vimuIntent
        }
        // DDD player
        if (player == "top.rootu.dddplayer") {
            val dddIntent = DDD.getIntent(player, torrent, index)
            if (dddIntent.resolveActivity(App.context.packageManager) != null)
                return dddIntent
        }
        // MX player
        if (player.contains("com.mxtech.videoplayer", true)) {
            val mxIntent = MX.getIntent(player, torrent, index)
            if (mxIntent.resolveActivity(App.context.packageManager) != null)
                return mxIntent
        }
        // UPlayer
        if (player.contains("com.uapplication.uplayer", true)) {
            val uIntent = UPlayer.getIntent(player, torrent, index)
            if (uIntent.resolveActivity(App.context.packageManager) != null)
                return uIntent
        }
        // Kodi player
        if (player.contains("org.xbmc.kodi", true) ||
            player.contains("net.kodinerds.maven.kodi", true) ||
            player.contains("org.xbmc.fandangos", true)) {
            val kodiIntent = Kodi.getIntent(player, torrent, index)
            if (kodiIntent.resolveActivity(App.context.packageManager) != null)
                return kodiIntent
        }
        return resolveIntent(intent, player)
    }

    fun getIntent(url: String, mime: String = "video/*", title: String? = null): Intent {
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(url.toUri(), mime)
        title?.let { intent.putExtra("title", it) }
        return resolveIntent(intent, Settings.getPlayer())
    }

    private fun resolveIntent(intent: Intent, player: String): Intent {
        if (player == "0" && intent.resolveActivity(App.context.packageManager) != null)
            return intent
        if (player.isNotEmpty() && player != "0") {
            intent.`package` = player
            if (intent.resolveActivity(App.context.packageManager) != null)
                return intent
            intent.`package` = null
        }
        // always ask / wrong package set
        return Intent.createChooser(intent, "")
    }

    fun getList(): List<Pair<String, String>> {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.fromFile(File(Environment.getExternalStorageDirectory().path, "file.mp4")), "video/*")
        var apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            App.context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        } else {
            App.context.packageManager.queryIntentActivities(intent, 0)
        } // PackageManager.MATCH_DEFAULT_ONLY
        val list = mutableListOf<Pair<String, String>>()
        list.add("" to App.context.getString(R.string.choose_player))
        list.add("0" to App.context.getString(R.string.default_player))
        for (a in apps) {
            val name = a.loadLabel(App.context.packageManager).toString() // ?: a.activityInfo.packageName
            if (!PLAYERS_BLACKLIST.contains(a.activityInfo.packageName.lowercase(Locale.getDefault()))) {
                list.add(a.activityInfo.packageName to name)
            }
        }

        intent.setDataAndType(Uri.fromFile(File(Environment.getExternalStorageDirectory().path, "file.mp3")), "audio/*")
        apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            App.context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        } else {
            App.context.packageManager.queryIntentActivities(intent, 0)
        } // PackageManager.MATCH_DEFAULT_ONLY
        for (a in apps) {
            val name = a.loadLabel(App.context.packageManager).toString() // ?: a.activityInfo.packageName
            if (!PLAYERS_BLACKLIST.contains(a.activityInfo.packageName.lowercase(Locale.getDefault()))) {
                list.add(a.activityInfo.packageName to name)
            }
        }

        return list.distinctBy { it.first }
    }

}
