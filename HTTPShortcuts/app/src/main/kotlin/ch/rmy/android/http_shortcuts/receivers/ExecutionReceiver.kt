package ch.rmy.android.http_shortcuts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity

class ExecutionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ExecuteActivity.IntentBuilder()
            .build(context)
            .apply {
                action = intent.action
                data = intent.data
                intent.extras?.let { extras ->
                    putExtras(extras)
                }
            }
            .startActivity(context)
    }
}
