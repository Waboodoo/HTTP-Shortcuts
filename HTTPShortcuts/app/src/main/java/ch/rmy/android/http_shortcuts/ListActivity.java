package ch.rmy.android.http_shortcuts;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images.Media;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.listeners.OnShortcutClickedListener;
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
public class ListActivity extends BaseActivity implements OnShortcutClickedListener {

    private static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

    @Bind(R.id.no_shortcuts)
    TextView emptyListText;
    @Bind(R.id.shortcut_list)
    RecyclerView shortcutList;
    @Bind(R.id.button_create_shortcut)
    FloatingActionButton createButton;

    private ShortcutStorage shortcutStorage;
    private ShortcutAdapter shortcutAdapter;

    private boolean shortcutPlacementMode = false;
    private boolean forwardedToEditor = false;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        shortcutStorage = new ShortcutStorage(this);
        shortcutAdapter = new ShortcutAdapter(this);

        shortcutAdapter.setOnShortcutClickListener(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        shortcutList.setLayoutManager(manager);
        shortcutList.addItemDecoration(new ShortcutListDecorator(this, R.drawable.list_divider));
        shortcutList.setAdapter(shortcutAdapter);
        registerForContextMenu(shortcutList);

        forwardedToEditor = false;

        ChangeLogDialog changeLog = new ChangeLogDialog(this, true);
        if (!changeLog.isPermanentlyHidden() && !changeLog.wasAlreadyShown()) {
            changeLog.show();
        }

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditorForCreation();
            }
        });
    }

    @Override
    protected void enableUpArrow() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        shortcutPlacementMode = Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction());

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
    public void onShortcutClicked(Shortcut shortcut, View view) {
        if (shortcutPlacementMode) {
            Intent intent = getShortcutPlacementIntent(shortcut);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            String action = PreferenceManager.getDefaultSharedPreferences(this).getString("click_behavior", "run");

            switch (action) {
                case "run":
                    HttpRequester.executeShortcut(this, shortcut, shortcutStorage);
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

    private void performContextMenuAction(int action, final Shortcut shortcut) {
        switch (action) {
            case 0: // place shortcut
                Intent shortcutPlacementIntent = getShortcutPlacementIntent(shortcut);
                sendBroadcast(shortcutPlacementIntent);
                return;
            case 1: // run
                HttpRequester.executeShortcut(this, shortcut, shortcutStorage);
                return;
            case 2: // edit
                editShortcut(shortcut);
                return;
            case 3: // move up
                shortcut.setPosition(shortcut.getPosition() - 1);
                shortcutStorage.storeShortcut(shortcut);
                updateShortcutList();
                return;
            case 4: // move down
                shortcut.setPosition(shortcut.getPosition() + 1);
                shortcutStorage.storeShortcut(shortcut);
                updateShortcutList();
                return;
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

                Toast.makeText(this, String.format(getText(R.string.shortcut_duplicated).toString(), shortcut.getName()), Toast.LENGTH_SHORT).show();

                return;
            case 6: // delete
                new AlertDialog.Builder(this).setTitle(R.string.confirm_delete_title).setMessage(R.string.confirm_delete_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                shortcutStorage.deleteShortcut(shortcut);
                                updateShortcutList();
                            }

                        }).setNegativeButton(android.R.string.no, null).show();
                return;
        }

        return;
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
        shortcutAdapter.updateShortcuts(shortcuts);

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
