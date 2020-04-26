package ch.rmy.android.http_shortcuts.exceptions

import org.liquidplayer.javascript.JSException

class JavaScriptException(private val e: JSException) : UserException() {
    override val message: String?
        get() = e.message
}