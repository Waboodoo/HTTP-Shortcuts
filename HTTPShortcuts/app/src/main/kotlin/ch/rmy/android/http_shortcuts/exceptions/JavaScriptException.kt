package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class JavaScriptException(
    override val message: String?,
    private val lineNumber: Int? = null,
) : UserException() {
    override fun getLocalizedMessage(context: Context): String =
        lineNumber
            ?.let { lineNumber ->
                context.getString(R.string.error_js_pattern_with_line_number, lineNumber, message)
            }
            ?: context.getString(R.string.error_js_pattern, message)
}
