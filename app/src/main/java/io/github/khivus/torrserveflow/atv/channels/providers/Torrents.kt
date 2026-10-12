package io.github.khivus.torrserveflow.atv.channels.providers

import io.github.khivus.torrserveflow.server.api.Api
import io.github.khivus.torrserveflow.server.models.torrent.Torrent

class Torrents : VideoProvider() {
    override fun get(): List<Torrent> {
        if (Api.echo().isNotEmpty())
            return Api.listTorrent()
        return emptyList()
    }
}