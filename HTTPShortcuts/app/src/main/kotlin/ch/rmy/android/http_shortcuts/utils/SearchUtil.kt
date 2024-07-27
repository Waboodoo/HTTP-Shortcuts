package ch.rmy.android.http_shortcuts.utils

import androidx.annotation.IntRange

object SearchUtil {
    fun normalizeToKeywords(input: String, @IntRange(from = 1) minLength: Int = 3): Set<String> =
        input
            .lowercase()
            .replace("[^a-z0-9]".toRegex(), " ")
            .split("\\s+".toRegex())
            .flatMap {
                // Best-effort hack to de-pluralize search terms. This will obviously only work well in English.
                if (it.endsWith("s")) {
                    listOf(it, it.dropLast(1))
                } else {
                    listOf(it)
                }
            }
            .filter { it.isNotBlank() && it.length >= minLength }
            .toSet()
}
