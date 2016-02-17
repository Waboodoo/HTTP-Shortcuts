package ch.rmy.android.http_shortcuts;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

public class ExecuteActivity extends Activity {

	public static final String ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.execute";
	public static final String EXTRA_SHORTCUT_ID = "id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_execute);

		long shortcutID = -1;
		Uri uri = getIntent().getData();
		if (uri != null) {
			try {
				String id = uri.getLastPathSegment();
				shortcutID = Long.parseLong(id);
			} catch (NumberFormatException e) {
			}
		}
		if (shortcutID == -1) {
			shortcutID = getIntent().getLongExtra(EXTRA_SHORTCUT_ID, -1); // for backwards compatibility
		}

		ShortcutStorage shortcutStorage = new ShortcutStorage(this);
		Shortcut shortcut = shortcutStorage.getShortcutByID(shortcutID);

		HttpRequester.executeShortcut(this, shortcut, shortcutStorage);

		finish();
	}

}
