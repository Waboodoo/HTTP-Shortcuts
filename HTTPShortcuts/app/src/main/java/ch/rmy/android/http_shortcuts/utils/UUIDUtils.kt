package ch.rmy.android.http_shortcuts.utils

import java.util.*

object UUIDUtils {

    fun create() = UUID.randomUUID().toString()

    fun toLong(uuid: String): Long = UUID.fromString(uuid).mostSignificantBits and Long.MAX_VALUE

}
