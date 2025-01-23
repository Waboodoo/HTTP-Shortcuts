package ch.rmy.android.http_shortcuts.http

import okhttp3.Request

fun buildRequest(method: String, url: String, builderAction: RequestBuilder.() -> Unit): Request =
    RequestBuilder(method, url).apply(builderAction).build()
