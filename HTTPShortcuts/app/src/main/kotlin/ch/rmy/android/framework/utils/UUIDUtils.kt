package ch.rmy.android.framework.utils

import java.lang.NumberFormatException
import java.util.UUID

object UUIDUtils {

    const val UUID_REGEX = "[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-4[0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}"

    fun newUUID() = UUID.randomUUID().toString()

    fun isUUID(input: String): Boolean =
        try {
            UUID.fromString(input)
            true
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: NumberFormatException) {
            false
        }
}
