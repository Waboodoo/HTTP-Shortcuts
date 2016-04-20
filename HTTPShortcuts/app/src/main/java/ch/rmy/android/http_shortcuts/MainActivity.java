package ch.rmy.android.http_shortcuts;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

/**
 * Main activity to list all shortcuts
 *
 * @author Roland Meyer
 */
public class MainActivity extends BaseActivity implements ListFragment.TabHost {

    private static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

    @Bind(R.id.button_create_shortcut)
    FloatingActionButton createButton;
    @Bind(R.id.view_pager)
    ViewPager viewPager;
    @Bind(R.id.tabs)
    TabLayout tabLayout;

    private Controller controller;
    private CategoryPagerAdapter adapter;

    private boolean shortcutPlacementMode = false;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        shortcutPlacementMode = Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction());

        controller = new Controller(this);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditorForCreation();
            }
        });
        setupViewPager();

        if (!shortcutPlacementMode) {
            checkChangeLog();
        }

        tabLayout.setVisibility(controller.getCategories().size() > 1 ? View.VISIBLE : View.GONE);
    }

    private void openEditorForCreation() {
        adapter.getItem(viewPager.getCurrentItem()).openEditorForCreation();
    }

    private void setupViewPager() {
        adapter = new CategoryPagerAdapter(getSupportFragmentManager());

        for (Category category : controller.getCategories()) {
            adapter.addFragment(ListFragment.newInstance(category, shortcutPlacementMode), category.getName());
        }

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
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

    @Override
    public void placeShortcutOnHomeScreen(Shortcut shortcut) {
        Intent shortcutPlacementIntent = getShortcutPlacementIntent(shortcut);
        sendBroadcast(shortcutPlacementIntent);
        showSnackbar(String.format(getString(R.string.shortcut_placed), shortcut.getName()));
    }

    @Override
    public void returnForHomeScreen(Shortcut shortcut) {
        Intent shortcutIntent = getShortcutPlacementIntent(shortcut);
        setResult(RESULT_OK, shortcutIntent);
        finish();
    }

}
