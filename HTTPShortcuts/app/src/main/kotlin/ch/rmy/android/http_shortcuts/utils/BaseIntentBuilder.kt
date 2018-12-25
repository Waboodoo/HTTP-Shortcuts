package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent

abstract class BaseIntentBuilder(val context: Context, clazz: Class<*>) {

    protected val intent: Intent = Intent(context, clazz)

    fun build() = intent

}