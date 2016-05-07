package ch.rmy.android.http_shortcuts.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import ch.rmy.android.http_shortcuts.realm.Controller;

public class ExecutionRetry extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            return;
        }

        Controller controller = null;
        try {
            controller = new Controller(context);

            /*if (isNetworkConnected(context)) { //TODO
                RealmResults<Shortcut> pendingShortcuts = controller.getShortcutsPendingExecution();

                for (Shortcut shortcut : pendingShortcuts) {
                    HttpRequester.executeShortcut(context, shortcut.getId(), controller);
                }
            }*/
        } finally {
            if (controller != null) {
                controller.destroy();
            }
        }
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
