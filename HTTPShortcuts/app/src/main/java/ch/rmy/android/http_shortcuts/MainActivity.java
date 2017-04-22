package ch.rmy.android.http_shortcuts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.adapters.CategoryPagerAdapter;
import ch.rmy.android.http_shortcuts.dialogs.ChangeLogDialog;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.utils.IntentUtil;
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder;
import ch.rmy.android.http_shortcuts.utils.SelectionMode;

/**
 * Main activity to list all shortcuts
 *
 * @author Roland Meyer
 */
public class MainActivity extends BaseActivity implements ListFragment.TabHost {

    public static final String EXTRA_SELECTION_ID = "ch.rmy.android.http_shortcuts.shortcut_id";
    public static final String EXTRA_SELECTION_NAME = "ch.rmy.android.http_shortcuts.shortcut_name";

    private final static int REQUEST_CREATE_SHORTCUT = 1;

    @Bind(R.id.button_create_shortcut)
    FloatingActionButton createButton;
    @Bind(R.id.view_pager)
    ViewPager viewPager;
    @Bind(R.id.tabs)
    TabLayout tabLayout;

    private Controller controller;
    private CategoryPagerAdapter adapter;

    private SelectionMode selectionMode = SelectionMode.NORMAL;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectionMode = SelectionMode.determineMode(getIntent());

        controller = destroyer.own(new Controller());

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateOptions();
            }
        });
        setupViewPager();

        if (selectionMode == SelectionMode.NORMAL) {
            checkChangeLog();
        }

        tabLayout.setTabTextColors(Color.WHITE, Color.WHITE);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
    }

    private void showCreateOptions() {
        new MenuDialogBuilder(this)
                .item(R.string.button_create_new, new MenuDialogBuilder.Action() {
                    @Override
                    public void execute() {
                        openEditorForCreation();
                    }
                })
                .item(R.string.button_curl_import, new MenuDialogBuilder.Action() {
                    @Override
                    public void execute() {
                        openCurlImport();
                    }
                }).show();
    }

    private void openEditorForCreation() {
        Intent intent = new Intent(this, EditorActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
    }

    private void setupViewPager() {
        adapter = new CategoryPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<Category> categories = controller.getCategories();
        tabLayout.setVisibility(categories.size() > 1 ? View.VISIBLE : View.GONE);
        if (viewPager.getCurrentItem() >= categories.size()) {
            viewPager.setCurrentItem(0);
        }
        adapter.setCategories(categories, selectionMode);
    }

    private void checkChangeLog() {
        ChangeLogDialog changeLog = new ChangeLogDialog(this, true);
        if (changeLog.shouldShow()) {
            changeLog.show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CREATE_SHORTCUT: {
                long shortcutId = intent.getLongExtra(EditorActivity.EXTRA_SHORTCUT_ID, 0);
                Shortcut shortcut = controller.getShortcutById(shortcutId);
                if (shortcut == null) {
                    return;
                }

                Category category;
                int currentCategory = viewPager.getCurrentItem();
                if (currentCategory < adapter.getCount()) {
                    ListFragment currentListFragment = adapter.getItem(currentCategory);
                    long categoryId = currentListFragment.getCategoryId();
                    category = controller.getCategoryById(categoryId);
                } else {
                    category = controller.getCategories().first();
                }
                controller.moveShortcut(shortcut, category);

                selectShortcut(shortcut);
                break;
            }
            case SettingsActivity.REQUEST_SETTINGS: {
                if (intent != null && intent.getBooleanExtra(SettingsActivity.EXTRA_THEME_CHANGED, false)) {
                    recreate();
                    openSettings();
                    overridePendingTransition(0, 0);
                }
                break;
            }
        }
    }

    @Override
    public void selectShortcut(Shortcut shortcut) {
        switch (selectionMode) {
            case HOME_SCREEN:
                returnForHomeScreen(shortcut);
                break;
            case PLUGIN:
                returnForPlugin(shortcut);
                break;
        }
    }

    private void returnForHomeScreen(Shortcut shortcut) {
        Intent shortcutIntent = IntentUtil.getShortcutPlacementIntent(getContext(), shortcut, true);
        setResult(RESULT_OK, shortcutIntent);
        finish();
    }

    private void returnForPlugin(Shortcut shortcut) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SELECTION_ID, shortcut.getId());
        intent.putExtra(EXTRA_SELECTION_NAME, shortcut.getName());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected int getNavigateUpIcon() {
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_categories:
                openCategoriesEditor();
                return true;
            case R.id.action_variables:
                openVariablesEditor();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, SettingsActivity.REQUEST_SETTINGS);
    }

    private void openCategoriesEditor() {
        Intent intent = new Intent(this, CategoriesActivity.class);
        startActivity(intent);
    }

    private void openVariablesEditor() {
        Intent intent = new Intent(this, VariablesActivity.class);
        startActivity(intent);
    }

    private void openCurlImport() {
        Intent intent = new Intent(this, CurlImportActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
    }

    @Override
    public void placeShortcutOnHomeScreen(Shortcut shortcut) {
        sendBroadcast(IntentUtil.getShortcutPlacementIntent(getContext(), shortcut, true));
        showSnackbar(String.format(getString(R.string.shortcut_placed), shortcut.getName()));
    }

    @Override
    public void removeShortcutFromHomeScreen(Shortcut shortcut) {
        sendBroadcast(IntentUtil.getShortcutPlacementIntent(getContext(), shortcut, false));
    }
}
