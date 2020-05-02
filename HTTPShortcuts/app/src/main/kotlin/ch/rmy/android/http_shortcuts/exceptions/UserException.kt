package ch.rmy.android.http_shortcuts.exceptions

import android.content.Context

abstract class UserException : Exception() {

    abstract fun getLocalizedMessage(context: Context): String

}