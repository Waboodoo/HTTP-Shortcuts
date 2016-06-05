package ch.rmy.android.http_shortcuts.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import ch.rmy.android.http_shortcuts.utils.Connectivity;

public class ServiceStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            return;
        }
        if (!Connectivity.isNetworkConnected(context)) {
            return;
        }
        context.startService(new Intent(context, ExecutionService.class));
    }

}
