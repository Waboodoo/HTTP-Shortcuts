package ch.rmy.android.framework.extensions

import android.net.Uri

val Uri.isWebUrl
    get() = scheme.run { equals("http", ignoreCase = true) || equals("https", ignoreCase = true) }
