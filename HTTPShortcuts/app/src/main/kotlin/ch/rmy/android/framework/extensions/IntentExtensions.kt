package ch.rmy.android.framework.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import ch.rmy.android.framework.ui.IntentBuilder

fun Intent.startActivity(activity: Activity) {
    activity.startActivity(this)
}

fun Intent.startActivity(fragment: Fragment) {
    fragment.startActivity(this)
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

fun IntentBuilder.startActivity(fragment: Fragment) {
    build(fragment.requireContext()).startActivity(fragment)
}

fun IntentBuilder.startActivity(context: Context) {
    build(context).startActivity(context)
}

fun <T : Any?> ActivityResultLauncher<T>.launch(options: ActivityOptionsCompat? = null) {
    launch(null, options)
}
