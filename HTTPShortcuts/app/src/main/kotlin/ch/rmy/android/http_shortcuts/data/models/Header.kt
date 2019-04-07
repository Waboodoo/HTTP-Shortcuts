package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValuePair
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Header(
    @PrimaryKey
    @Required
    var id: String = newUUID(),
    @Required
    override var key: String = "",
    @Required
    override var value: String = ""
) : RealmObject(), KeyValuePair {

    fun isSameAs(other: Header) = other.key == key && other.value == value

    companion object {

        val SUGGESTED_KEYS = arrayOf(
            "Accept",
            "Accept-Charset",
            "Accept-Encoding",
            "Accept-Language",
            "Accept-Datetime",
            "Authorization",
            "Cache-Control",
            "Connection",
            "Cookie",
            "Content-Length",
            "Content-MD5",
            "Content-Type",
            "Date",
            "Expect",
            "Forwarded",
            "From",
            "Host",
            "If-Match",
            "If-Modified-Since",
            "If-None-Match",
            "If-Range",
            "If-Unmodified-Since",
            "Max-Forwards",
            "Origin",
            "Pragma",
            "Proxy-Authorization",
            "Range",
            "Referer",
            "TE",
            "User-Agent",
            "Upgrade",
            "Via",
            "Warning"
        )

    }

}
