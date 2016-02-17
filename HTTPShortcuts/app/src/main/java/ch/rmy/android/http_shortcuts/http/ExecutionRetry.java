package ch.rmy.android.http_shortcuts.http;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

public class ExecutionRetry extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (isNetworkConnected(context)) {
			ShortcutStorage shortcutStorage = new ShortcutStorage(context);
			List<Shortcut> pendingShortcuts = shortcutStorage.getShortcutsPendingExecution();

			for (Shortcut shortcut : pendingShortcuts) {
				HttpRequester.executeShortcut(context, shortcut, shortcutStorage);
			}
		}
	}

	private boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null;
	}

}
