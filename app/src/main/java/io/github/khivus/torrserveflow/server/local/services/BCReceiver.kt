package io.github.khivus.torrserveflow.server.local.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.khivus.torrserveflow.atv.Utils
import io.github.khivus.torrserveflow.server.local.TorrService
import io.github.khivus.torrserveflow.settings.Settings

class BCReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            if (Settings.isBootStart()) {
                TorrService.start()
            }
            Utils.updateAtvCards()
        }
    }
}