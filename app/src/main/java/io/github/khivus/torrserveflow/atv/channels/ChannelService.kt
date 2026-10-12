package io.github.khivus.torrserveflow.atv.channels

import android.util.Log
import io.github.khivus.torrserveflow.BuildConfig
import io.github.khivus.torrserveflow.R
import io.github.khivus.torrserveflow.app.App
import io.github.khivus.torrserveflow.atv.channels.providers.Provider

object ChannelService {
    fun updateChannels() {
        if (BuildConfig.DEBUG)
            Log.i("*****", "ChannelService: updateChannels()")

        with(App.context) {
            try {
                ChannelProvider("Torrents", getString(R.string.torrents)).update(Provider.get(Provider.Torrents))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}