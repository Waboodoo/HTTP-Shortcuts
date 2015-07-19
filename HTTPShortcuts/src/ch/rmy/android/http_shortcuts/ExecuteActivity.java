package ch.rmy.android.http_shortcuts;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.shortcuts.PostParameter;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

public class ExecuteActivity extends Activity {

	public static final String ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.execute";
	public static final String EXTRA_SHORTCUT_ID = "id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_execute);

		long shortcutID = getIntent().getLongExtra(EXTRA_SHORTCUT_ID, 0);
		ShortcutStorage shortcutStorage = new ShortcutStorage(this);
		Shortcut shortcut = shortcutStorage.getShortcutByID(shortcutID);

		if (shortcut != null) {

			final List<PostParameter> parameters;
			if (shortcut.getMethod() == Shortcut.METHOD_POST) {
				parameters = shortcutStorage.getPostParametersByID(shortcutID);
			} else {
				parameters = null;
			}

			HttpRequester.executeShortcut(this, shortcut, parameters);
		} else {
			Toast.makeText(this, R.string.shortcut_not_found, Toast.LENGTH_LONG).show();
		}

		finish();
	}
}
