package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import org.json.JSONException
import org.liquidplayer.javascript.JSException

class JavaScriptException(override val message: String?, private val error: Exception? = null) : UserException() {

    constructor(e: JSException) : this(e.message, e)

    constructor(e: JSONException) : this(e.message, e)

    override fun getLocalizedMessage(context: Context): String =
        getLineNumber()
            ?.let { lineNumber ->
                context.getString(R.string.error_js_pattern_with_line_number, lineNumber, message)
            }
            ?: context.getString(R.string.error_js_pattern, message)

    private fun getLineNumber(): Int? =
        (error as? JSException)?.error
            ?.stack()
            ?.split("\n")
            ?.getOrNull(1)
            ?.trim(')')
            ?.split(':')
            ?.dropLast(1)
            ?.lastOrNull()
            ?.toInt()

}