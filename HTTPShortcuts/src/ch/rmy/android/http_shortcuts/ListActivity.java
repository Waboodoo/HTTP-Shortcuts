package ch.rmy.android.http_shortcuts;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images.Media;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.shortcuts.Header;
import ch.rmy.android.http_shortcuts.shortcuts.PostParameter;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutAdapter;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

/**
 * Main activity to list all shortcuts
 * 
 * @author Roland Meyer
 */
public class ListActivity extends Activity implements OnItemClickListener {

	private static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

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

		emptyListText = (TextView) findViewById(R.id.no_shortcuts);
		forwardedToEditor = false;

		ChangeLogDialog changeLog = new ChangeLogDialog(this, true);
		if (!changeLog.isPermanentlyHidden() && !changeLog.wasAlreadyShown()) {
			changeLog.show();
		}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_activity_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_create_shortcut:
			openEditorForCreation();
			return true;
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void openEditorForCreation() {
		Intent intent = new Intent(this, EditorActivity.class);
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
			String action = PreferenceManager.getDefaultSharedPreferences(this).getString("click_behavior", "run");

			if (action.equals("run")) {
				HttpRequester.executeShortcut(this, shortcut, shortcutStorage);
			} else if (action.equals("edit")) {
				editShortcut(shortcut);
			} else if (action.equals("menu")) {
				view.showContextMenu();
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Shortcut shortcut = shortcutStorage.getShortcuts().get(info.position);

		menu.setHeaderTitle(shortcut.getName());

		menu.add(0, 0, 0, R.string.action_place);
		menu.add(0, 1, 0, R.string.action_run);
		menu.add(0, 2, 0, R.string.action_edit);
		menu.add(0, 3, 0, R.string.action_move_up);
		menu.add(0, 4, 0, R.string.action_move_down);
		menu.add(0, 5, 0, R.string.action_duplicate);
		menu.add(0, 6, 0, R.string.action_delete);
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
			HttpRequester.executeShortcut(this, shortcut, shortcutStorage);
			return true;
		case 2: // edit
			editShortcut(shortcut);
			return true;
		case 3: // move up
			shortcut.setPosition(shortcut.getPosition() - 1);
			shortcutStorage.storeShortcut(shortcut);
			updateShortcutList();
			return true;
		case 4: // move down
			shortcut.setPosition(shortcut.getPosition() + 1);
			shortcutStorage.storeShortcut(shortcut);
			updateShortcutList();
			return true;
		case 5: // duplicate
			String newName = String.format(getText(R.string.copy).toString(), shortcut.getName());
			Shortcut newShortcut = shortcut.duplicate(newName);
			long newId = shortcutStorage.storeShortcut(newShortcut);

			List<PostParameter> oldParameters = shortcutStorage.getPostParametersByID(shortcut.getID());
			List<PostParameter> newParameters = new ArrayList<PostParameter>();
			for (PostParameter oldParameter : oldParameters) {
				newParameters.add(new PostParameter(0, oldParameter.getKey(), oldParameter.getValue()));
			}
			shortcutStorage.storePostParameters(newId, newParameters);

			List<Header> oldHeaders = shortcutStorage.getHeadersByID(shortcut.getID());
			List<Header> newHeaders = new ArrayList<Header>();
			for (Header oldHeader : oldHeaders) {
				newHeaders.add(new Header(0, oldHeader.getKey(), oldHeader.getValue()));
			}
			shortcutStorage.storeHeaders(newId, newHeaders);

			updateShortcutList();
			return true;
		case 6: // delete
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

		shortcutIntent.setData(ContentUris.withAppendedId(Uri.fromParts("content", getPackageName(), null), shortcut.getID()));

		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.getName());
		if (shortcut.getIconName() != null) {

			Uri uri = shortcut.getIconURI(this);
			Bitmap icon;
			try {
				icon = Media.getBitmap(this.getContentResolver(), uri);
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);

			} catch (Exception e) {
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), Shortcut.DEFAULT_ICON));
			}
		} else {
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), Shortcut.DEFAULT_ICON));
		}

		addIntent.setAction(ACTION_INSTALL_SHORTCUT);

		return addIntent;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == EditorActivity.EDIT_SHORTCUT && resultCode == RESULT_OK) {
			updateShortcutList();

			if (shortcutPlacementMode) {
				long shortcutID = intent.getLongExtra(EditorActivity.EXTRA_SHORTCUT_ID, 0);
				Shortcut shortcut = shortcutStorage.getShortcutByID(shortcutID);
				Intent shortcutIntent = getShortcutPlacementIntent(shortcut);
				setResult(RESULT_OK, shortcutIntent);
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

	private void editShortcut(Shortcut shortcut) {
		Intent intent = new Intent(this, EditorActivity.class);
		intent.putExtra(EditorActivity.EXTRA_SHORTCUT_ID, shortcut.getID());
		startActivityForResult(intent, EditorActivity.EDIT_SHORTCUT);
	}

}
