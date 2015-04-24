package ch.rmy.android.http_shortcuts;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
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

		int shortcutID = getIntent().getIntExtra(EXTRA_SHORTCUT_ID, 0);
		ShortcutStorage shortcutStorage = new ShortcutStorage(this);
		Shortcut shortcut = shortcutStorage.getShortcutByID(shortcutID);

		if (shortcut != null) {
			HttpRequester.executeShortcut(this, shortcut);
		} else {
			Toast.makeText(this, R.string.shortcut_not_found, Toast.LENGTH_LONG).show();
		}

		finish();
	}
}
