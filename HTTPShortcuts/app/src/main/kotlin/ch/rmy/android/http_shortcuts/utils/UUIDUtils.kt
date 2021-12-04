package ch.rmy.android.http_shortcuts.utils

import ch.rmy.android.http_shortcuts.extensions.logException
import java.util.UUID

object UUIDUtils {

    const val UUID_REGEX = "[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-4[0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}"

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
