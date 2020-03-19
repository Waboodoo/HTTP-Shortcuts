package ch.rmy.android.http_shortcuts.extensions

import android.net.Uri

val Uri.isWebUrl
    get() = scheme.equals("http", ignoreCase = true) || scheme.equals("https", ignoreCase = true)