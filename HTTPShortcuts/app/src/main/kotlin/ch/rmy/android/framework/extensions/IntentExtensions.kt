package ch.rmy.android.framework.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import ch.rmy.android.framework.ui.IntentBuilder
import java.io.Serializable

inline fun createIntent(block: Intent.() -> Unit): Intent =
    Intent().apply(block)

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

fun IntentBuilder.startActivity(context: Context) {
    build(context).startActivity(context)
}

fun ActivityResultLauncher<Unit?>.launch(options: ActivityOptionsCompat? = null) {
    launch(Unit, options)
}

@Suppress("DEPRECATION")
inline fun <reified T : Any?> Intent.getParcelable(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getParcelableExtra(key, T::class.java) else getParcelableExtra(key)

@Suppress("DEPRECATION")
inline fun <reified T : Any?> Intent.getParcelableList(key: String): List<T>? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getParcelableArrayListExtra(key, T::class.java) else getParcelableArrayListExtra(key)

@Suppress("DEPRECATION")
inline fun <reified T : Serializable?> Intent.getSerializable(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getSerializableExtra(key, T::class.java) else getSerializableExtra(key) as? T
