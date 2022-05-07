package ch.rmy.android.http_shortcuts.plugin

import android.content.Context
import javax.inject.Inject

class TaskerUtil
@Inject
constructor(
    private val context: Context,
) {
    fun isTaskerAvailable(): Boolean =
        TaskerIntent.isTaskerInstalled(context)
}
