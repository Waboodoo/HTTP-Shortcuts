package ch.rmy.android.framework.ui

import android.content.Context
import android.content.Intent

interface IntentBuilder {
    fun build(context: Context): Intent
}
