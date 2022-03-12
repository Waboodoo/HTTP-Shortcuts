package ch.rmy.android.framework.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import ch.rmy.android.framework.extensions.startActivity

interface IntentBuilder {

    fun build(context: Context): Intent

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
