package ch.rmy.android.http_shortcuts.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import ch.rmy.android.http_shortcuts.extensions.startActivity

abstract class BaseIntentBuilder(context: Context, clazz: Class<*>) {

    protected val intent: Intent = Intent(context, clazz)

    fun build() = intent

    fun startActivity(activity: Activity, requestCode: Int? = null) {
        build().startActivity(activity, requestCode)
    }

    fun startActivity(fragment: Fragment, requestCode: Int? = null) {
        build().startActivity(fragment, requestCode)
    }

    fun startActivity(context: Context, requestCode: Int? = null) {
        build().startActivity(context, requestCode)
    }

}