package ch.rmy.android.framework.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import ch.rmy.android.framework.extensions.startActivity

abstract class BaseIntentBuilder(private val clazz: Class<*>) {

    protected val intent: Intent = Intent()

    open fun build(context: Context) = intent
        .setClass(context, clazz)

    fun startActivity(activity: Activity, requestCode: Int? = null) {
        build(activity).startActivity(activity, requestCode)
    }

    fun startActivity(fragment: Fragment, requestCode: Int? = null) {
        build(fragment.requireContext()).startActivity(fragment, requestCode)
    }

    fun startActivity(context: Context, requestCode: Int? = null) {
        build(context).startActivity(context, requestCode)
    }
}
