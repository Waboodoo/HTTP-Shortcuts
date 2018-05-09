package ch.rmy.android.http_shortcuts.realm.models

import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValuePair
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Header : RealmObject(), KeyValuePair {

    @PrimaryKey
    @Required
    var id: String = ""

    @Required
    override var key: String = ""
    @Required
    override var value: String = ""

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

        fun createNew(key: String, value: String): Header {
            val header = Header()
            header.id = UUIDUtils.create()
            header.key = key
            header.value = value
            return header
        }
    }

    fun isSameAs(other: Header) = other.key == key && other.value == value

}
