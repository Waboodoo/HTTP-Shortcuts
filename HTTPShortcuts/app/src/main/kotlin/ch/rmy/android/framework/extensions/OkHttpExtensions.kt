package ch.rmy.android.framework.extensions

import okhttp3.Response

fun Response.isSuccessfulOrRedirect() =
    code in 200..399
