package ch.rmy.android.framework.ui

import android.content.Context
import android.content.Intent

abstract class BaseIntentBuilder(private val clazz: Class<*>) : IntentBuilder {

    protected val intent: Intent = Intent()

    override fun build(context: Context) = intent
        .setClass(context, clazz)

    override fun toString() =
        "IntentBuilder(${clazz.simpleName})"
}
