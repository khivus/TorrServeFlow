package io.github.khivus.torrserveflow.server.api

import com.google.gson.Gson
import io.github.khivus.torrserveflow.settings.BTSets

open class Request(val action: String) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

class SettingsReq(
    action: String,
    val Sets: BTSets
) : Request(action)

class TorrentReq(
    action: String,
    val hash: String = "",
    val link: String = "",
    val title: String = "",
    val poster: String = "",
    val category: String = "",
    val data: String = "",
    val save_to_db: Boolean = false,
) : Request(action)

class ViewedReq(
    action: String,
    val hash: String = "",
    val file_index: Int = -1
) : Request(action)

data class Viewed(
    val hash: String,
    val file_index: Int
)