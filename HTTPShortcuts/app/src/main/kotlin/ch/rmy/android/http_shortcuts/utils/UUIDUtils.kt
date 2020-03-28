package ch.rmy.android.http_shortcuts.utils

import ch.rmy.android.http_shortcuts.extensions.logException
import java.util.*

object UUIDUtils {

    fun newUUID() = UUID.randomUUID().toString()

    fun toLong(uuid: String): Long =
        try {
            UUID.fromString(uuid).mostSignificantBits and Long.MAX_VALUE
        } catch (e: IllegalArgumentException) {
            uuid.toLongOrNull() ?: run {
                val exception = IllegalArgumentException("Cannot convert string to long: $uuid")
                logException(exception)
                throw exception
            }
        }

    fun isUUID(input: String): Boolean =
        try {
            UUID.fromString(input)
            true
        } catch (e: IllegalArgumentException) {
            false
        }

}
