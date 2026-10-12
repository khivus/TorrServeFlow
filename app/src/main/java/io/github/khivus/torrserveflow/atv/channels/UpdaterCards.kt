package io.github.khivus.torrserveflow.atv.channels

import io.github.khivus.torrserveflow.atv.Utils
import kotlin.concurrent.thread

object UpdaterCards {

    private val lock = Any()
    private var isUpdate = false

    fun updateCards() {
        thread {
            if (!Utils.isAndroidTV)
                return@thread

            synchronized(lock) {
                if (isUpdate)
                    return@thread
                isUpdate = true
            }

            ChannelService.updateChannels()
            isUpdate = false
        }
    }
}