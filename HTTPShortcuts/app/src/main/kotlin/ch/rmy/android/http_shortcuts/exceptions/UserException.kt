package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context

abstract class UserException : Exception() {

    abstract fun getLocalizedMessage(context: Context): String

    companion object {
        fun create(messageFactory: Context.() -> String) = object : UserException() {
            override fun getLocalizedMessage(context: Context): String =
                messageFactory(context)
        }
    }
}
