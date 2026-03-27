package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import ch.rmy.android.http_shortcuts.exceptions.UserException

fun userError(message: String): Nothing = throw UserException.create { message }

fun userError(message: Context.() -> String): Nothing = throw UserException.create(message)
