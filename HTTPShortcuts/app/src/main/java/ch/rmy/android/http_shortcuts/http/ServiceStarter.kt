package ch.rmy.android.http_shortcuts.http

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

import ch.rmy.android.http_shortcuts.utils.Connectivity

class ServiceStarter : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION != intent.action || !Connectivity.isNetworkConnected(context)) {
            return
        }
        context.startService(Intent(context, ExecutionService::class.java))
    }

}
