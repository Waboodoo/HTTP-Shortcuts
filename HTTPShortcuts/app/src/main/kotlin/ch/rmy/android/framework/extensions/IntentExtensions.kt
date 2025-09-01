package ch.rmy.android.framework.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import ch.rmy.android.framework.ui.IntentBuilder

fun Intent.startActivity(activity: Activity) {
    activity.startActivity(this)
}

fun Intent.startActivity(context: Context) {
    when (context) {
        is Activity -> startActivity(context)
        else -> context.startActivity(this)
    }
}

fun IntentBuilder.startActivity(activity: Activity) {
    build(activity).startActivity(activity)
}

@Suppress("DEPRECATION")
inline fun <reified T : Any?> Intent.getParcelable(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getParcelableExtra(key, T::class.java) else getParcelableExtra(key)
