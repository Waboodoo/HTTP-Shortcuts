package ch.rmy.android.framework.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import kotlin.reflect.KClass

abstract class BaseIntentBuilder(private val clazz: Class<out Activity>) : IntentBuilder {

    constructor(clazz: KClass<out Activity>) : this(clazz.java)

    protected val intent: Intent = Intent()

    override fun build(context: Context) = intent
        .setClass(context, clazz)

    override fun toString() =
        "IntentBuilder(${clazz.simpleName})"
}
