package ch.rmy.android.http_shortcuts;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutAdapter;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

public class ListActivity extends Activity implements OnClickListener, OnItemClickListener {

	private ShortcutStorage shortcutStorage;
	private ShortcutAdapter shortcutAdapter;
	private TextView emptyListText;

	private boolean shortcutPlacementMode = false;
	private boolean forwardedToEditor = false;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(getResources().getColor(R.color.dark_blue));
		}

		shortcutStorage = new ShortcutStorage(this);
		shortcutAdapter = new ShortcutAdapter(this);

		ListView shortcutList = (ListView) findViewById(R.id.shortcut_list);
		shortcutList.setOnItemClickListener(this);
		shortcutList.setAdapter(shortcutAdapter);
		registerForContextMenu(shortcutList);

		Button createButton = (Button) findViewById(R.id.add_shortcut_button);
		createButton.setOnClickListener(this);

		emptyListText = (TextView) findViewById(R.id.no_shortcuts);
		forwardedToEditor = false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
			shortcutPlacementMode = true;
		} else {
			shortcutPlacementMode = false;
		}

		updateShortcutList();
	}

	@Override
	public void onClick(View v) {
		openEditorForCreation();
	}

	private void openEditorForCreation() {
		Intent intent = new Intent(this, EditorActivity.class);
		intent.putExtra(EditorActivity.EXTRA_SHORTCUT, shortcutStorage.createShortcut());
		startActivityForResult(intent, EditorActivity.EDIT_SHORTCUT);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Shortcut shortcut = shortcutStorage.getShortcuts().get(position);

		if (shortcutPlacementMode) {
			Intent intent = getShortcutPlacementIntent(shortcut);
			setResult(RESULT_OK, intent);
			finish();
		} else {
			view.showContextMenu();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Shortcut shortcut = shortcutStorage.getShortcuts().get(info.position);

		menu.setHeaderTitle(shortcut.getName());
		if (shortcut.getIconName() != null) {
			menu.setHeaderIcon(Drawable.createFromPath(getFileStreamPath(shortcut.getIconName()).getAbsolutePath()));
		} else {
			menu.setHeaderIcon(Shortcut.DEFAULT_ICON);
		}

		menu.add(0, 0, 0, R.string.action_place);
		menu.add(0, 1, 0, R.string.action_run);
		menu.add(0, 2, 0, R.string.action_edit);
		menu.add(0, 3, 0, R.string.action_duplicate);
		menu.add(0, 4, 0, R.string.action_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		final Shortcut shortcut = shortcutStorage.getShortcuts().get(info.position);

		switch (item.getItemId()) {
		case 0: // place shortcut
			Intent shortcutPlacementIntent = getShortcutPlacementIntent(shortcut);
			sendBroadcast(shortcutPlacementIntent);
			return true;
		case 1: // run
			HttpRequester.executeShortcut(this, shortcut);
			return true;
		case 2: // edit
			Intent intent = new Intent(this, EditorActivity.class);
			intent.putExtra(EditorActivity.EXTRA_SHORTCUT, shortcut);
			startActivityForResult(intent, EditorActivity.EDIT_SHORTCUT);
			return true;
		case 3: // duplicate
			Shortcut newShortcut = shortcut.duplicate();
			shortcutStorage.storeShortcut(newShortcut);
			updateShortcutList();
			return true;
		case 4: // delete
			new AlertDialog.Builder(this).setTitle(R.string.confirm_delete_title).setMessage(R.string.confirm_delete_message)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							shortcutStorage.deleteShortcut(shortcut);
							updateShortcutList();
						}

					}).setNegativeButton(android.R.string.no, null).show();
			return true;
		}

		return false;
	}

	private Intent getShortcutPlacementIntent(Shortcut shortcut) {

		Intent shortcutIntent = new Intent(this, ExecuteActivity.class);
		shortcutIntent.setAction(ExecuteActivity.ACTION_EXECUTE_SHORTCUT);
		shortcutIntent.putExtra(ExecuteActivity.EXTRA_SHORTCUT_ID, shortcut.getID());
		// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.getName());
		if (shortcut.getIconName() != null) {
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeFile(getFileStreamPath(shortcut.getIconName()).getAbsolutePath()));
		} else {
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher));
		}

		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

		return addIntent;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == EditorActivity.EDIT_SHORTCUT && resultCode == RESULT_OK) {
			Shortcut shortcut = (Shortcut) intent.getParcelableExtra(EditorActivity.EXTRA_SHORTCUT);
			shortcutStorage.storeShortcut(shortcut);

			updateShortcutList();

			if (shortcutPlacementMode) {
				Intent shortuctIntent = getShortcutPlacementIntent(shortcut);
				setResult(RESULT_OK, shortuctIntent);
				finish();
			}
		}
	}

	private void updateShortcutList() {
		List<Shortcut> shortcuts = shortcutStorage.getShortcuts();
		shortcutAdapter.clear();
		shortcutAdapter.addAll(shortcuts);

		if (shortcuts.isEmpty()) {

			if (!forwardedToEditor && shortcutPlacementMode) {
				forwardedToEditor = true;
				openEditorForCreation();
			}

			emptyListText.setVisibility(View.VISIBLE);
		} else {
			forwardedToEditor = true;
			emptyListText.setVisibility(View.GONE);
		}
	}

}
