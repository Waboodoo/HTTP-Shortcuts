package ch.rmy.android.http_shortcuts.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

fun Intent.startActivity(activity: Activity, requestCode: Int? = null) {
    if (requestCode != null) {
        activity.startActivityForResult(this, requestCode)
    } else {
        activity.startActivity(this)
    }
}

fun Intent.startActivity(fragment: Fragment, requestCode: Int? = null) {
    if (requestCode != null) {
        fragment.startActivityForResult(this, requestCode)
    } else {
        fragment.startActivity(this)
    }
}

fun Intent.startActivity(context: Context, requestCode: Int? = null) {
    when {
        context is Activity -> startActivity(context, requestCode)
        requestCode != null -> throw IllegalArgumentException("Cannot start activity for result without activity context")
        else -> context.startActivity(this)
    }
}