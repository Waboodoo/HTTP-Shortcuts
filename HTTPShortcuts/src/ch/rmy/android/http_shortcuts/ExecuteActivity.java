package ch.rmy.android.http_shortcuts;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.shortcuts.Header;
import ch.rmy.android.http_shortcuts.shortcuts.PostParameter;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

public class ExecuteActivity extends Activity {

	private static final String TAG = ExecuteActivity.class.getName();

	public static final String ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.execute";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_execute);

		long shortcutID;
		try {
			String id = getIntent().getData().getLastPathSegment();
			shortcutID = Long.parseLong(id);
		} catch (NumberFormatException e) {
			shortcutID = -1;
		}

		ShortcutStorage shortcutStorage = new ShortcutStorage(this);
		Shortcut shortcut = shortcutStorage.getShortcutByID(shortcutID);

		if (shortcut != null) {

			final List<PostParameter> parameters;
			if (shortcut.getMethod().equals(Shortcut.METHOD_POST)) {
				parameters = shortcutStorage.getPostParametersByID(shortcutID);
			} else {
				parameters = null;
			}

			final List<Header> headers = shortcutStorage.getHeadersByID(shortcutID);

			HttpRequester.executeShortcut(this, shortcut, parameters, headers);
		} else {
			Toast.makeText(this, R.string.shortcut_not_found, Toast.LENGTH_LONG).show();
			Log.e(TAG, "shortcut id: " + shortcutID);
		}

		finish();
	}
}
