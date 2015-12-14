package ch.rmy.android.http_shortcuts.http;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import ch.rmy.android.http_shortcuts.shortcuts.Header;
import ch.rmy.android.http_shortcuts.shortcuts.PostParameter;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

public class ExecutionRetry extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (isNetworkConnected(context)) {
			ShortcutStorage shortcutStorage = new ShortcutStorage(context);
			List<Shortcut> pendingShortcuts = shortcutStorage.getShortcutsPendingExecution();

			for (Shortcut shortcut : pendingShortcuts) {
				runShortcut(context, shortcutStorage, shortcut);
			}
		}
	}

	private boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null;
	}

	private void runShortcut(Context context, ShortcutStorage shortcutStorage, Shortcut shortcut) {
		final List<PostParameter> parameters;
		if (shortcut.getMethod().equals(Shortcut.METHOD_GET)) {
			parameters = null;
		} else {
			parameters = shortcutStorage.getPostParametersByID(shortcut.getID());
		}

		final List<Header> headers = shortcutStorage.getHeadersByID(shortcut.getID());

		HttpRequester.executeShortcut(context, shortcut, parameters, headers);
	}

}
