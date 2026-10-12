package io.github.khivus.torrserveflow.ext

import java.net.URLEncoder

fun String.urlEncode(): String = URLEncoder.encode(this, "utf8")