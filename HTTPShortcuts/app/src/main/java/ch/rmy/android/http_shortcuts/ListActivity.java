package ch.rmy.android.http_shortcuts;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.listeners.OnShortcutClickedListener;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import io.realm.RealmResults;

/**
 * Main activity to list all shortcuts
 *
 * @author Roland Meyer
 */
public class ListActivity extends BaseActivity implements OnShortcutClickedListener {

    private static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

    @Bind(R.id.shortcut_list)
    RecyclerView shortcutList;
    @Bind(R.id.button_create_shortcut)
    FloatingActionButton createButton;

    private Controller controller;

    private boolean shortcutPlacementMode = false;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        controller = new Controller(this);
        RealmResults<Shortcut> shortcuts = controller.getShortcuts();
        ShortcutAdapter adapter = new ShortcutAdapter(this, shortcuts);

        adapter.setOnShortcutClickListener(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        shortcutList.setLayoutManager(manager);
        shortcutList.setHasFixedSize(true);
        shortcutList.addItemDecoration(new ShortcutListDecorator(this, R.drawable.list_divider));
        shortcutList.setAdapter(adapter);
        registerForContextMenu(shortcutList);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditorForCreation();
            }
        });

        shortcutPlacementMode = Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction());
        if (shortcuts.isEmpty() && shortcutPlacementMode) {
            openEditorForCreation();
            // TODO: Store state that forward has occured
        } else {
            checkChangeLog();
        }
    }

    private void checkChangeLog() {
        ChangeLogDialog changeLog = new ChangeLogDialog(this, true);
        if (!changeLog.isPermanentlyHidden() && !changeLog.wasAlreadyShown()) {
            changeLog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.destroy();
    }

    @Override
    protected int getNavigateUpIcon() {
        return 0;
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
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openEditorForCreation() {
        Intent intent = new Intent(this, EditorActivity.class);
        startActivityForResult(intent, EditorActivity.EDIT_SHORTCUT);
    }

    @Override
    public void onShortcutClicked(Shortcut shortcut, View view) {
        if (shortcutPlacementMode) {
            Intent intent = getShortcutPlacementIntent(shortcut);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            String action = PreferenceManager.getDefaultSharedPreferences(this).getString("click_behavior", "run");

            switch (action) {
                case "run":
                    executeShortcut(shortcut);
                    break;
                case "edit":
                    editShortcut(shortcut);
                    break;
                case "menu":
                    showContextMenu(shortcut);
                    break;
            }
        }
    }

    @Override
    public void onShortcutLongClicked(Shortcut shortcut, View view) {
        showContextMenu(shortcut);
    }

    private void showContextMenu(final Shortcut shortcut) {
        (new MaterialDialog.Builder(this))
                .title(shortcut.getName())
                .items(R.array.context_menu_items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        performContextMenuAction(which, shortcut);
                    }
                })
                .show();
    }

    private void performContextMenuAction(int action, Shortcut shortcut) {
        switch (action) {
            case 0:
                placeShortcutOnHomeScreen(shortcut);
                return;
            case 1:
                executeShortcut(shortcut);
                return;
            case 2:
                editShortcut(shortcut);
                return;
            case 3:
                controller.moveShortcut(shortcut, Controller.DIRECTION_UP);
                return;
            case 4:
                controller.moveShortcut(shortcut, Controller.DIRECTION_DOWN);
                return;
            case 5:
                duplicateShortcut(shortcut);
                return;
            case 6:
                showDeleteDialog(shortcut);
                return;
        }

        return;
    }

    private void placeShortcutOnHomeScreen(Shortcut shortcut) {
        Intent shortcutPlacementIntent = getShortcutPlacementIntent(shortcut);
        sendBroadcast(shortcutPlacementIntent);
        showSnackbar(String.format(getString(R.string.shortcut_placed).toString(), shortcut.getName()));
    }

    private void executeShortcut(Shortcut shortcut) {
        HttpRequester.executeShortcut(this, shortcut.getId(), controller);
    }

    private void editShortcut(Shortcut shortcut) {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra(EditorActivity.EXTRA_SHORTCUT_ID, shortcut.getId());
        startActivityForResult(intent, EditorActivity.EDIT_SHORTCUT);
    }

    private void duplicateShortcut(Shortcut shortcut) {
        String newName = String.format(getText(R.string.copy).toString(), shortcut.getName());
        controller.duplicateShortcut(shortcut, newName);
        showSnackbar(String.format(getText(R.string.shortcut_duplicated).toString(), shortcut.getName()));
    }

    private void showDeleteDialog(final Shortcut shortcut) {
        (new MaterialDialog.Builder(this))
                .content(R.string.confirm_delete_message)
                .positiveText(R.string.dialog_delete)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deleteShortcut(shortcut);
                    }
                })
                .negativeText(R.string.dialog_cancel)
                .show();
    }

    private void deleteShortcut(Shortcut shortcut) {
        controller.deleteShortcut(shortcut);
        showSnackbar(String.format(getText(R.string.shortcut_deleted).toString(), shortcut.getName()));
    }

    private Intent getShortcutPlacementIntent(Shortcut shortcut) {
        Intent shortcutIntent = new Intent(this, ExecuteActivity.class);
        shortcutIntent.setAction(ExecuteActivity.ACTION_EXECUTE_SHORTCUT);

        Uri uri = ContentUris.withAppendedId(Uri.fromParts("content", getPackageName(), null), shortcut.getId());
        shortcutIntent.setData(uri);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.getName());
        if (shortcut.getIconName() != null) {

            Uri iconUri = shortcut.getIconURI(this);
            Bitmap icon;
            try {
                icon = Media.getBitmap(this.getContentResolver(), iconUri);
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

            if (shortcutPlacementMode) {
                long shortcutId = intent.getLongExtra(EditorActivity.EXTRA_SHORTCUT_ID, 0);
                Shortcut shortcut = controller.getShortcutById(shortcutId);
                Intent shortcutIntent = getShortcutPlacementIntent(shortcut);
                setResult(RESULT_OK, shortcutIntent);
                finish();
            }
        }
    }

}
