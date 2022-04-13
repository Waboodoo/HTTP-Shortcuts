package ch.rmy.android.http_shortcuts.plugin

import android.content.Context

class TaskerUtil(private val context: Context) {
    fun isTaskerAvailable(): Boolean =
        TaskerIntent.isTaskerInstalled(context)
}
