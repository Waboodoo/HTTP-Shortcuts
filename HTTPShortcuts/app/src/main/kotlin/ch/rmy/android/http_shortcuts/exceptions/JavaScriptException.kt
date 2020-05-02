package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import org.liquidplayer.javascript.JSException

class JavaScriptException(private val e: JSException) : UserException() {

    override val message: String?
        get() = e.message

    override fun getLocalizedMessage(context: Context): String =
        context.getString(R.string.error_js_pattern, message)

}