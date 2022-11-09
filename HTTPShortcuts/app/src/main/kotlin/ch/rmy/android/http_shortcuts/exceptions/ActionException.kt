package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context

class ActionException(private val messageProvider: Context.() -> String) : UserException() {

    override fun getLocalizedMessage(context: Context): String =
        messageProvider.invoke(context)
}
