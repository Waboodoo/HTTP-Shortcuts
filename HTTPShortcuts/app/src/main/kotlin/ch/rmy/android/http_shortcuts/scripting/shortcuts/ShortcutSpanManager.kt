package ch.rmy.android.http_shortcuts.scripting.shortcuts

import android.text.Spannable
import android.text.Spanned
import androidx.annotation.ColorInt
import java.util.regex.Pattern

object ShortcutSpanManager {

    private const val JS_PLACEHOLDER_REGEX = """/\*\[shortcut]\*/"([^"]+)"/\*\[/shortcut]\*/"""

    private val JS_PATTERN = Pattern.compile(JS_PLACEHOLDER_REGEX)

    fun applyShortcutFormattingToJS(text: Spannable, shortcutPlaceholderProvider: ShortcutPlaceholderProvider, @ColorInt color: Int) {
        text.getSpans(0, text.length, JSShortcutSpan::class.java)
            .forEach { span ->
                text.removeSpan(span)
            }
        val matcher = JS_PATTERN.matcher(text)
        while (matcher.find()) {
            val variableId = matcher.group(1)!!
            val placeholder = shortcutPlaceholderProvider.findPlaceholderById(variableId)
            val variableKey = placeholder?.name ?: "???"
            text.setSpan(JSShortcutSpan(color, variableKey), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

}