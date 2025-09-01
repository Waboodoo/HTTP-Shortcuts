package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.regex.Pattern
import java.util.regex.Pattern.quote

object IconUtil {

    private const val ICON_SCALING_FACTOR = 4

    private const val CUSTOM_ICON_NAME_PREFIX = "custom-icon_"
    private const val CUSTOM_ICON_NAME_SUFFIX = ".png"
    const val CUSTOM_CIRCULAR_ICON_NAME_SUFFIX = "_circle"
    private const val CUSTOM_ICON_NAME_ALTERNATIVE_SUFFIX = ".jpg"

    private const val CUSTOM_ICON_MAX_FILE_SIZE = 8 * 1024 * 1024

    private val CUSTOM_ICON_NAME_REGEX = "${quote(CUSTOM_ICON_NAME_PREFIX)}([A-Za-z0-9_-]{1,36})" +
        "(${quote(CUSTOM_ICON_NAME_SUFFIX)}|${quote(CUSTOM_ICON_NAME_ALTERNATIVE_SUFFIX)})"
    private val CUSTOM_ICON_NAME_PATTERN = CUSTOM_ICON_NAME_REGEX.toPattern(Pattern.CASE_INSENSITIVE)

    private var iconSizeCached: Int? = null

    fun isCustomIconName(string: String) =
        string.matches(CUSTOM_ICON_NAME_REGEX.toRegex())

    fun generateCustomIconName(circular: Boolean): String =
        "${CUSTOM_ICON_NAME_PREFIX}x" +
            "${Instant.now().toEpochMilli()}" +
            (if (circular) CUSTOM_CIRCULAR_ICON_NAME_SUFFIX else "") +
            CUSTOM_ICON_NAME_SUFFIX

    fun extractCustomIconNames(string: String): Set<String> =
        buildSet {
            val matcher = CUSTOM_ICON_NAME_PATTERN.matcher(string)
            while (matcher.find()) {
                add(matcher.group())
            }
        }

    suspend fun getCustomIconNamesInApp(context: Context): List<String> =
        withContext(Dispatchers.IO) {
            context.filesDir
                .listFiles { file ->
                    file.name.matches(CUSTOM_ICON_NAME_REGEX.toRegex()) && file.length() < CUSTOM_ICON_MAX_FILE_SIZE
                }
                ?.map { it.name }
                ?: emptyList()
        }
}
