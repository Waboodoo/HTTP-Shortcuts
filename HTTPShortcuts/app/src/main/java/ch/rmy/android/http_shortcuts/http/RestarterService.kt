package ch.rmy.android.http_shortcuts.http

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RestarterService : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ExecutionService.schedule(context)
    }

}
