package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import org.json.JSONException
import org.liquidplayer.javascript.JSException

class JavaScriptException(override val message: String?) : UserException() {

    constructor(e: JSException) : this(e.message)

    constructor(e: JSONException) : this(e.message)

    override fun getLocalizedMessage(context: Context): String =
        context.getString(R.string.error_js_pattern, message)

}