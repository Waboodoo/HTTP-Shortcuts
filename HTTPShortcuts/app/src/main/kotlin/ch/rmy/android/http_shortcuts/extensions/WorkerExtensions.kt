package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import androidx.work.ListenableWorker

val ListenableWorker.context: Context
    get() = applicationContext
