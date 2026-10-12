package io.github.khivus.torrserveflow.ui.fragments.main.update.apk

class ApkVersions : ArrayList<ApkVersion>()

data class ApkVersion(
    val desc: String,
    val link: String,
    val version: String,
    val versionInt: Int
)