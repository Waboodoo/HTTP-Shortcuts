package ch.rmy.android.http_shortcuts.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.adapters.CategoryPagerAdapter
import ch.rmy.android.http_shortcuts.dialogs.ChangeLogDialog
import ch.rmy.android.http_shortcuts.http.ExecutionService
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.utils.SelectionMode
import ch.rmy.android.http_shortcuts.utils.consume
import ch.rmy.android.http_shortcuts.utils.visible
import kotterknife.bindView

class MainActivity : BaseActivity(), ListFragment.TabHost {

    internal val createButton: FloatingActionButton by bindView(R.id.button_create_shortcut)
    internal val viewPager: ViewPager by bindView(R.id.view_pager)
    internal val tabLayout: TabLayout by bindView(R.id.tabs)

    private val controller by lazy { destroyer.own(Controller()) }
    private var adapter: CategoryPagerAdapter? = null

    private var selectionMode = SelectionMode.NORMAL

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectionMode = SelectionMode.determineMode(intent)

        createButton.setOnClickListener { showCreateOptions() }
        setupViewPager()

        if (selectionMode === SelectionMode.NORMAL) {
            checkChangeLog()
        }

        tabLayout.setTabTextColors(Color.WHITE, Color.WHITE)
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE)

        ExecutionService.start(context)
    }

    private fun showCreateOptions() {
        MenuDialogBuilder(context)
                .item(R.string.button_create_new, this::openEditorForCreation)
                .item(R.string.button_curl_import, this::openCurlImport)
                .show()
    }

    private fun openEditorForCreation() {
        val intent = Intent(context, EditorActivity::class.java)
        startActivityForResult(intent, REQUEST_CREATE_SHORTCUT)
    }

    private fun setupViewPager() {
        adapter = CategoryPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onStart() {
        super.onStart()
        val categories = controller.categories
        tabLayout.visible = categories.size > 1
        if (viewPager.currentItem >= categories.size) {
            viewPager.currentItem = 0
        }
        adapter!!.setCategories(categories, selectionMode)
    }

    private fun checkChangeLog() {
        val changeLog = ChangeLogDialog(context, true)
        if (changeLog.shouldShow()) {
            changeLog.show()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return
        }
        when (requestCode) {
            REQUEST_CREATE_SHORTCUT -> {
                val shortcutId = intent.getLongExtra(EditorActivity.EXTRA_SHORTCUT_ID, 0)
                val shortcut = controller.getShortcutById(shortcutId) ?: return

                val category: Category
                val currentCategory = viewPager.currentItem
                if (currentCategory < adapter!!.count) {
                    val currentListFragment = adapter!!.getItem(currentCategory)
                    val categoryId = currentListFragment.categoryId
                    category = controller.getCategoryById(categoryId)!!
                } else {
                    category = controller.categories.first()!!
                }
                controller.moveShortcut(shortcut, category)

                selectShortcut(shortcut)
            }
            SettingsActivity.REQUEST_SETTINGS -> {
                if (intent.getBooleanExtra(SettingsActivity.EXTRA_THEME_CHANGED, false)) {
                    recreate()
                    openSettings()
                    overridePendingTransition(0, 0)
                }
            }
        }
    }

    override fun selectShortcut(shortcut: Shortcut) {
        when (selectionMode) {
            SelectionMode.HOME_SCREEN -> returnForHomeScreen(shortcut)
            SelectionMode.PLUGIN -> returnForPlugin(shortcut)
            SelectionMode.NORMAL -> {
            }
        }
    }

    private fun returnForHomeScreen(shortcut: Shortcut) {
        val shortcutIntent = if (LauncherShortcutManager.supportsPinning(context)) {
            LauncherShortcutManager.createShortcutPinIntent(context, shortcut)
        } else {
            IntentUtil.getShortcutPlacementIntent(context, shortcut, true)
        }
        setResult(Activity.RESULT_OK, shortcutIntent)
        finish()
    }

    private fun returnForPlugin(shortcut: Shortcut) {
        val intent = Intent()
        intent.putExtra(EXTRA_SELECTION_ID, shortcut.id)
        intent.putExtra(EXTRA_SELECTION_NAME, shortcut.name)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override val navigateUpIcon = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> consume { openSettings() }
        R.id.action_categories -> consume { openCategoriesEditor() }
        R.id.action_variables -> consume { openVariablesEditor() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun openSettings() {
        val intent = Intent(context, SettingsActivity::class.java)
        startActivityForResult(intent, SettingsActivity.REQUEST_SETTINGS)
    }

    private fun openCategoriesEditor() {
        val intent = Intent(context, CategoriesActivity::class.java)
        startActivity(intent)
    }

    private fun openVariablesEditor() {
        val intent = Intent(context, VariablesActivity::class.java)
        startActivity(intent)
    }

    private fun openCurlImport() {
        val intent = Intent(context, CurlImportActivity::class.java)
        startActivityForResult(intent, REQUEST_CREATE_SHORTCUT)
    }

    override fun placeShortcutOnHomeScreen(shortcut: Shortcut) {
        if (LauncherShortcutManager.supportsPinning(context)) {
            LauncherShortcutManager.pinShortcut(context, shortcut)
        } else {
            sendBroadcast(IntentUtil.getShortcutPlacementIntent(context, shortcut, true))
            showSnackbar(String.format(getString(R.string.shortcut_placed), shortcut.name))
        }
    }

    override fun removeShortcutFromHomeScreen(shortcut: Shortcut) {
        sendBroadcast(IntentUtil.getShortcutPlacementIntent(context, shortcut, false))
    }

    companion object {

        const val EXTRA_SELECTION_ID = "ch.rmy.android.http_shortcuts.shortcut_id"
        const val EXTRA_SELECTION_NAME = "ch.rmy.android.http_shortcuts.shortcut_name"

        private const val REQUEST_CREATE_SHORTCUT = 1

    }
}
