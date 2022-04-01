package ch.rmy.android.http_shortcuts.activities.main

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContract
import androidx.viewpager.widget.ViewPager
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.launch
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.restartWithoutAnimation
import ch.rmy.android.framework.extensions.titleView
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.ui.BaseFragment
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesActivity
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.activities.misc.CurlImportActivity
import ch.rmy.android.http_shortcuts.activities.settings.importexport.ImportExportActivity
import ch.rmy.android.http_shortcuts.activities.settings.settings.SettingsActivity
import ch.rmy.android.http_shortcuts.activities.widget.WidgetSettingsActivity
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.databinding.ActivityMainBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import com.google.android.material.appbar.AppBarLayout

class MainActivity : BaseActivity(), Entrypoint {

    private val openSettings = registerForActivityResult(SettingsActivity.OpenSettings) { result ->
        if (result.themeChanged) {
            recreate()
            openSettings()
            overridePendingTransition(0, 0)
        } else if (result.appLocked) {
            viewModel.onAppLocked()
        }
    }

    private val openCurlImport = registerForActivityResult(CurlImportActivity.ImportFromCurl) { curlCommand ->
        curlCommand?.let(viewModel::onCurlCommandSubmitted)
    }

    private val openWidgetSettings = registerForActivityResult(WidgetSettingsActivity.OpenWidgetSettings) { result ->
        if (result != null) {
            viewModel.onWidgetSettingsSubmitted(
                shortcutId = result.shortcutId,
                showLabel = result.showLabel,
                labelColor = result.labelColor,
            )
        } else {
            finish()
        }
    }

    private val openShortcutEditor = registerForActivityResult(ShortcutEditorActivity.OpenShortcutEditor) { shortcutId ->
        shortcutId?.let(viewModel::onShortcutCreated)
    }

    private val openCategories = registerForActivityResult(CategoriesActivity.OpenCategories) { categoriesChanged ->
        if (categoriesChanged) {
            restartWithoutAnimation()
        }
    }

    private val openImportExport = registerForActivityResult(ImportExportActivity.OpenImportExport) { categoriesChanged ->
        if (categoriesChanged) {
            restartWithoutAnimation()
        }
    }

    private val viewModel: MainViewModel by bindViewModel()

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CategoryPagerAdapter

    private var menuItemSettings: MenuItem? = null
    private var menuItemImportExport: MenuItem? = null
    private var menuItemAbout: MenuItem? = null
    private var menuItemCategories: MenuItem? = null
    private var menuItemVariables: MenuItem? = null
    private var menuItemUnlock: MenuItem? = null

    private var activeCategoryIndex: Int = 0
        set(value) {
            if (field != value) {
                field = value
                binding.viewPager.currentItem = value
            }
        }

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize(
            MainViewModel.InitData(
                selectionMode = determineMode(intent.action),
                initialCategoryId = intent?.extras?.getString(EXTRA_CATEGORY_ID),
                widgetId = WidgetManager.getWidgetIdFromIntent(intent),
            )
        )
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun determineMode(action: String?) = when (action) {
        Intent.ACTION_CREATE_SHORTCUT -> SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT
        AppWidgetManager.ACTION_APPWIDGET_CONFIGURE -> SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT
        ACTION_SELECT_SHORTCUT_FOR_PLUGIN -> SelectionMode.PLUGIN
        else -> SelectionMode.NORMAL
    }

    private fun initViews() {
        binding = applyBinding(ActivityMainBinding.inflate(layoutInflater))

        setupViewPager()

        binding.tabs.applyTheme(themeHelper)
        binding.buttonCreateShortcut.applyTheme(themeHelper)
    }

    private fun setupViewPager() {
        adapter = CategoryPagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = adapter
        binding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                viewModel.onSwitchedToCategory(position)
            }
        })
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    private fun initUserInputBindings() {
        binding.buttonCreateShortcut.setOnClickListener {
            viewModel.onCreateShortcutButtonClicked()
        }

        toolbar!!.titleView?.setOnClickListener {
            viewModel.onToolbarTitleClicked()
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.loadingIndicator.visible = false
            setTitle(viewState.toolbarTitleLocalizable)
            adapter.setCategories(viewState.categoryTabItems, viewState.selectionMode)
            setTabLongPressListener()
            activeCategoryIndex = viewState.activeCategoryIndex
            binding.tabs.visible = viewState.isTabBarVisible
            binding.buttonCreateShortcut.visible = viewState.isCreateButtonVisible
            menuItemSettings?.isVisible = viewState.isRegularMenuButtonVisible
            menuItemImportExport?.isVisible = viewState.isRegularMenuButtonVisible
            menuItemAbout?.isVisible = viewState.isRegularMenuButtonVisible
            menuItemCategories?.isVisible = viewState.isRegularMenuButtonVisible
            menuItemVariables?.isVisible = viewState.isRegularMenuButtonVisible
            menuItemUnlock?.isVisible = viewState.isUnlockButtonVisible
            setToolbarScrolling(viewState.isToolbarScrollable)
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    private fun setToolbarScrolling(isScrollable: Boolean) {
        (toolbar?.layoutParams as? AppBarLayout.LayoutParams?)?.scrollFlags =
            if (isScrollable) {
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                    AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
            } else 0
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is MainEvent.OpenCurlImport -> openCurlImport.launch()
            is MainEvent.OpenWidgetSettings -> openWidgetSettings.launch {
                shortcut(event.shortcut)
            }
            is MainEvent.OpenShortcutEditor -> openShortcutEditor.launch { event.intentBuilder }
            is MainEvent.OpenCategories -> openCategories.launch()
            is MainEvent.OpenSettings -> openSettings()
            is MainEvent.OpenImportExport -> openImportExport.launch()
            else -> super.handleEvent(event)
        }
    }

    private fun openSettings() {
        openSettings.launch()
    }

    override val navigateUpIcon = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        menuItemSettings = menu.findItem(R.id.action_settings)
        menuItemImportExport = menu.findItem(R.id.action_import_export)
        menuItemAbout = menu.findItem(R.id.action_about)
        menuItemCategories = menu.findItem(R.id.action_categories)
        menuItemVariables = menu.findItem(R.id.action_variables)
        menuItemUnlock = menu.findItem(R.id.action_unlock)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> consume { viewModel.onSettingsButtonClicked() }
        R.id.action_import_export -> consume { viewModel.onImportExportButtonClicked() }
        R.id.action_about -> consume { viewModel.onAboutButtonClicked() }
        R.id.action_categories -> consume { viewModel.onCategoriesButtonClicked() }
        R.id.action_variables -> consume { viewModel.onVariablesButtonClicked() }
        R.id.action_unlock -> consume { viewModel.onUnlockButtonClicked() }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments
            .filter { it.isResumed }
            .filterIsInstance(BaseFragment::class.java)
            .forEach { fragment ->
                if (fragment.onBackPressed()) {
                    return
                }
            }
        super.onBackPressed()
    }

    private fun setTabLongPressListener() {
        (0..binding.tabs.tabCount)
            .mapNotNull { binding.tabs.getTabAt(it)?.view }
            .forEach {
                it.setOnLongClickListener {
                    consume {
                        viewModel.onTabLongClicked()
                    }
                }
            }
    }

    object SelectShortcut : ActivityResultContract<Unit, SelectShortcut.Result?>() {
        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(context, MainActivity::class.java)
                .setAction(ACTION_SELECT_SHORTCUT_FOR_PLUGIN)

        override fun parseResult(resultCode: Int, intent: Intent?): Result? =
            if (resultCode == Activity.RESULT_OK && intent != null) {
                Result(
                    shortcutId = intent.getStringExtra(EXTRA_SELECTION_ID)!!,
                    shortcutName = intent.getStringExtra(EXTRA_SELECTION_NAME)!!,
                )
            } else null

        data class Result(val shortcutId: String, val shortcutName: String)
    }

    class IntentBuilder : BaseIntentBuilder(MainActivity::class.java) {
        init {
            intent.action = Intent.ACTION_VIEW
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        fun categoryId(categoryId: String) = also {
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
        }
    }

    companion object {

        private const val ACTION_SELECT_SHORTCUT_FOR_PLUGIN = "ch.rmy.android.http_shortcuts.plugin"

        const val EXTRA_SELECTION_ID = "ch.rmy.android.http_shortcuts.shortcut_id"
        const val EXTRA_SELECTION_NAME = "ch.rmy.android.http_shortcuts.shortcut_name"
        private const val EXTRA_CATEGORY_ID = "ch.rmy.android.http_shortcuts.category_id"
    }
}
