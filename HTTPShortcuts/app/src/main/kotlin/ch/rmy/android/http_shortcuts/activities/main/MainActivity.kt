package ch.rmy.android.http_shortcuts.activities.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesActivity
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.activities.misc.CurlImportActivity
import ch.rmy.android.http_shortcuts.activities.settings.SettingsActivity
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.ChangeLogDialog
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.dialogs.NetworkRestrictionWarningDialog
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.http.ExecutionScheduler
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.SelectionMode
import ch.rmy.curlcommand.CurlCommand
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotterknife.bindView

class MainActivity : BaseActivity(), ListFragment.TabHost {

    private val viewModel: MainViewModel by bindViewModel()

    private lateinit var adapter: CategoryPagerAdapter

    private val selectionMode by lazy {
        SelectionMode.determineMode(intent.action)
    }

    private val categories by lazy {
        viewModel.getCategories()
    }

    // Views
    private val createButton: FloatingActionButton by bindView(R.id.button_create_shortcut)
    private val viewPager: ViewPager by bindView(R.id.view_pager)
    private val tabLayout: TabLayout by bindView(R.id.tabs)

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (categories.size <= 1) {
            (toolbar?.layoutParams as? AppBarLayout.LayoutParams?)?.scrollFlags = 0
        }

        initViews()

        if (selectionMode === SelectionMode.NORMAL) {
            showStartupDialogs()
        }

        ExecutionScheduler.schedule(context)
        LauncherShortcutManager.updateAppShortcuts(context, categories)
    }

    private fun initViews() {
        createButton.setOnClickListener { showCreateOptions() }
        setupViewPager()
        tabLayout.applyTheme(themeHelper)
        createButton.applyTheme(themeHelper)
        bindViewsToViewModel()
    }

    private fun bindViewsToViewModel() {
        viewModel.appLockedSource.observe(this, Observer { isLocked ->
            createButton.visible = !isLocked
            invalidateOptionsMenu()
        })

        viewModel.getCategories().observe(this, Observer { categories ->
            tabLayout.visible = categories.size > 1
            if (viewPager.currentItem >= categories.size) {
                viewPager.currentItem = 0
            }
        })
    }

    private fun showCreateOptions() {
        DialogBuilder(context)
            .title(R.string.title_create_new_shortcut_options_dialog)
            .item(R.string.button_create_new, ::openEditorForCreation)
            .item(R.string.button_create_browser_shortcut, ::openEditorForBrowserShortcutCreation)
            .item(R.string.button_curl_import, ::openCurlImport)
            .showIfPossible()
    }

    private fun openEditorForCreation() {
        val categoryId = adapter.getItem(viewPager.currentItem).categoryId
        ShortcutEditorActivity.IntentBuilder(context)
            .categoryId(categoryId)
            .build()
            .startActivity(this, REQUEST_CREATE_SHORTCUT)
    }

    private fun openEditorForBrowserShortcutCreation() {
        val categoryId = adapter.getItem(viewPager.currentItem).categoryId
        ShortcutEditorActivity.IntentBuilder(context)
            .categoryId(categoryId)
            .browserShortcut(true)
            .build()
            .startActivity(this, REQUEST_CREATE_SHORTCUT)
    }

    private fun setupViewPager() {
        adapter = CategoryPagerAdapter(supportFragmentManager, selectionMode)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        viewModel.getCategories().observe(this, Observer { categories ->
            adapter.setCategories(categories)
        })
    }

    private fun showStartupDialogs() {
        ChangeLogDialog(context, whatsNew = true)
            .showIfNeeded()
            .andThen(
                NetworkRestrictionWarningDialog(context)
                    .showIfNeeded()
            )
            .subscribe()
            .attachTo(destroyer)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return
        }
        when (requestCode) {
            REQUEST_CREATE_SHORTCUT_FROM_CURL -> {
                val curlCommand = intent.getSerializableExtra(CurlImportActivity.EXTRA_CURL_COMMAND) as CurlCommand
                openEditorWithCurlCommand(curlCommand)
            }
            REQUEST_CREATE_SHORTCUT -> {
                val shortcutId = intent.getStringExtra(ShortcutEditorActivity.RESULT_SHORTCUT_ID)
                selectShortcut(shortcutId)
            }
            REQUEST_SETTINGS -> {
                if (intent.getBooleanExtra(SettingsActivity.EXTRA_THEME_CHANGED, false)) {
                    recreate()
                    openSettings()
                    overridePendingTransition(0, 0)
                } else if (intent.getBooleanExtra(SettingsActivity.EXTRA_APP_LOCKED, false)) {
                    showSnackbar(R.string.message_app_locked)
                }
            }
        }
    }

    private fun openEditorWithCurlCommand(curlCommand: CurlCommand) {
        val categoryId = adapter.getItem(viewPager.currentItem).categoryId
        ShortcutEditorActivity.IntentBuilder(context)
            .categoryId(categoryId)
            .curlCommand(curlCommand)
            .build()
            .startActivity(this, REQUEST_CREATE_SHORTCUT)
    }

    private fun selectShortcut(shortcutId: String) {
        selectShortcut(viewModel.getShortcutById(shortcutId) ?: return)
    }

    override fun selectShortcut(shortcut: Shortcut) {
        when (selectionMode) {
            SelectionMode.HOME_SCREEN -> returnForHomeScreen(shortcut)
            SelectionMode.PLUGIN -> returnForPlugin(shortcut)
            SelectionMode.NORMAL -> Unit
        }
    }

    private fun returnForHomeScreen(shortcut: Shortcut) {
        if (LauncherShortcutManager.supportsPinning(context)) {
            DialogBuilder(context)
                .title(R.string.title_select_placement_method)
                .message(R.string.description_select_placement_method)
                .positive(R.string.label_placement_method_default) {
                    finishWithPlacement(
                        LauncherShortcutManager.createShortcutPinIntent(context, shortcut)
                    )
                }
                .negative(R.string.label_placement_method_legacy) {
                    finishWithPlacement(IntentUtil.getShortcutPlacementIntent(context, shortcut, true))
                }
                .showIfPossible()
        } else {
            finishWithPlacement(IntentUtil.getShortcutPlacementIntent(context, shortcut, true))
        }
    }

    private fun finishWithPlacement(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
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
        if (viewModel.isAppLocked()) {
            menuInflater.inflate(R.menu.locked_main_activity_menu, menu)
        } else {
            menuInflater.inflate(R.menu.main_activity_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> consume { openSettings() }
        R.id.action_categories -> consume { openCategoriesEditor() }
        R.id.action_variables -> consume { openVariablesEditor() }
        R.id.action_unlock -> consume { openAppUnlockDialog() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun openSettings() {
        SettingsActivity.IntentBuilder(context)
            .build()
            .startActivity(this, REQUEST_SETTINGS)
    }

    private fun openCategoriesEditor() {
        CategoriesActivity.IntentBuilder(context)
            .build()
            .startActivity(this)
    }

    private fun openVariablesEditor() {
        VariablesActivity.IntentBuilder(context)
            .build()
            .startActivity(this)
    }

    private fun openAppUnlockDialog(showError: Boolean = false) {
        DialogBuilder(context)
            .title(R.string.dialog_title_unlock_app)
            .message(if (showError) R.string.dialog_text_unlock_app_retry else R.string.dialog_text_unlock_app)
            .positive(R.string.button_unlock_app)
            .textInput(inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD) { input ->
                unlockApp(input)
            }
            .negative(R.string.dialog_cancel)
            .showIfPossible()
    }

    private fun unlockApp(password: String) {
        viewModel.removeAppLock(password)
            .subscribe({
                if (viewModel.isAppLocked()) {
                    openAppUnlockDialog(showError = true)
                } else {
                    showSnackbar(R.string.message_app_unlocked)
                }
            }, { e ->
                showSnackbar(R.string.error_generic, long = true)
                logException(e)
            })
            .attachTo(destroyer)
    }

    private fun openCurlImport() {
        CurlImportActivity.IntentBuilder(context)
            .build()
            .startActivity(this, REQUEST_CREATE_SHORTCUT_FROM_CURL)
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

    override fun isAppLocked() = viewModel.isAppLocked()

    companion object {

        const val EXTRA_SELECTION_ID = "ch.rmy.android.http_shortcuts.shortcut_id"
        const val EXTRA_SELECTION_NAME = "ch.rmy.android.http_shortcuts.shortcut_name"

        private const val REQUEST_CREATE_SHORTCUT = 1
        private const val REQUEST_CREATE_SHORTCUT_FROM_CURL = 2
        private const val REQUEST_SETTINGS = 3

    }
}
