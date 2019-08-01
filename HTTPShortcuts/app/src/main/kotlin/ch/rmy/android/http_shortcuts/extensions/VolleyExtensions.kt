package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import com.android.volley.VolleyError

fun VolleyError.getShortcutResponse(): ShortcutResponse? =
    networkResponse?.let {
        ShortcutResponse(
            url = null,
            headers = it.headers,
            statusCode = it.statusCode,
            data = it.data
        )
    }