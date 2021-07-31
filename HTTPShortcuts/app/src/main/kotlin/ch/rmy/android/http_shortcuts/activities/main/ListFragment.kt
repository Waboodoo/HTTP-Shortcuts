package ch.rmy.android.http_shortcuts.activities.main

import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.CurlExportDialog
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.dialogs.ShortcutInfoDialog
import ch.rmy.android.http_shortcuts.exceptions.CanceledByUserException
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.http.ExecutionScheduler
import ch.rmy.android.http_shortcuts.import_export.CurlExporter
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.ExportUI
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.utils.GridLayoutManager
import ch.rmy.android.http_shortcuts.utils.SelectionMode
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.curlcommand.CurlConstructor
import kotterknife.bindView

class ListFragment : BaseFragment() {

    val categoryId by lazy {
        args.getString(ARG_CATEGORY_ID) ?: ""
    }

    private val selectionMode by lazy {
        args.getSerializable(ARG_SELECTION_MODE) as SelectionMode
    }

    private var isInMovingMode = false
        set(value) {
            if (field != value) {
                field = value
                tabHost?.isInMovingMode = value
                if (value) {
                    showSnackbar(R.string.message_moving_enabled, long = true)
                }
            }
        }

    private val exportUI by lazy {
        destroyer.own(ExportUI(requireActivity()))
    }

    private val viewModel: ShortcutListViewModel by bindViewModel()

    private lateinit var categories: ListLiveData<Category>
    private lateinit var categoryData: LiveData<Category?>
    private lateinit var shortcuts: ListLiveData<Shortcut>
    private lateinit var pendingShortcuts: ListLiveData<PendingExecution>

    private var layoutType: String? = null
    private var adapter: BaseShortcutAdapter? = null

    override val layoutResource = R.layout.fragment_list

    // Views
    private val shortcutList: RecyclerView by bindView(R.id.shortcut_list)
    private val backgroundView: ImageView by bindView(R.id.background)

    private val wallpaper: Drawable? by lazy {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.drawable
        } catch (e: SecurityException) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.categoryId = categoryId

        categories = viewModel.getCategories()
        categoryData = viewModel.getCategory()
        shortcuts = viewModel.getShortcuts()
        pendingShortcuts = viewModel.getPendingShortcuts()
    }

    override fun setupViews() {
        shortcutList.setHasFixedSize(true)
        bindViewsToViewModel()
    }

    private fun bindViewsToViewModel() {
        categoryData.observe(this) {
            updateViews()
        }
        pendingShortcuts.observe(this) { pendingShortcuts ->
            adapter?.setPendingShortcuts(pendingShortcuts)
        }
        shortcuts.observe(this) {
            updateEmptyState()
        }
    }

    private fun updateViews() {
        val layoutType = categoryData.value?.layoutType ?: return

        if (layoutType != this.layoutType || adapter == null) {
            this.layoutType = layoutType
            initDragOrdering(layoutType)
            adapter?.destroy()

            val adapter = when (layoutType) {
                Category.LAYOUT_GRID -> ShortcutGridAdapter(requireContext(), shortcuts)
                else -> ShortcutListAdapter(requireContext(), shortcuts)
            }
            val manager = when (layoutType) {
                Category.LAYOUT_GRID -> GridLayoutManager(requireContext())
                else -> LinearLayoutManager(context)
            }
            this.adapter = destroyer.own(adapter)
            destroyer.own {
                this.adapter = null
            }

            adapter.clickListener = ::onItemClicked
            adapter.longClickListener = ::onItemLongClicked

            shortcutList.layoutManager = manager
            shortcutList.adapter = adapter
            updateEmptyState()
        }

        categoryData.value?.background?.let {
            adapter?.textColor = if (it == Category.BACKGROUND_TYPE_WHITE) {
                BaseShortcutAdapter.TextColor.DARK
            } else {
                BaseShortcutAdapter.TextColor.BRIGHT
            }
            updateBackground(it)
        }
    }

    private fun initDragOrdering(layoutType: String) {
        val dragOrderingHelper = DragOrderingHelper(allowHorizontalDragging = layoutType == Category.LAYOUT_GRID) { isInMovingMode }
        dragOrderingHelper.attachTo(shortcutList)
        dragOrderingHelper.positionChangeSource
            .concatMapCompletable { (oldPosition, newPosition) ->
                val shortcut = shortcuts[oldPosition]!!
                viewModel.moveShortcut(shortcut.id, newPosition)
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun updateBackground(background: String) {
        backgroundView.apply {
            when (background) {
                Category.BACKGROUND_TYPE_BLACK -> {
                    setImageDrawable(null)
                    setBackgroundColor(color(requireContext(), R.color.activity_background_dark))
                }
                Category.BACKGROUND_TYPE_WALLPAPER -> {
                    wallpaper
                        ?.also {
                            setImageDrawable(it)
                        }
                        ?: run {
                            setImageDrawable(null)
                            setBackgroundColor(color(requireContext(), R.color.activity_background))
                            adapter?.textColor = BaseShortcutAdapter.TextColor.DARK
                        }
                }
                else -> {
                    setImageDrawable(null)
                    setBackgroundColor(color(requireContext(), R.color.activity_background))
                }
            }
        }
    }

    private fun updateEmptyState() {
        (shortcutList.layoutManager as? GridLayoutManager)?.setEmpty(shortcuts.isEmpty())
    }

    private fun onItemClicked(shortcutData: LiveData<Shortcut?>) {
        val shortcut = shortcutData.value ?: return
        if (isInMovingMode) {
            showSnackbar(R.string.message_moving_enabled)
            return
        }
        when (selectionMode) {
            SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT,
            SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT,
            SelectionMode.PLUGIN,
            -> tabHost?.selectShortcut(shortcut)
            else -> {
                if (tabHost?.isAppLocked() == true) {
                    executeShortcut(shortcut)
                    return
                }
                when (Settings(requireContext()).clickBehavior) {
                    Settings.CLICK_BEHAVIOR_RUN -> executeShortcut(shortcut)
                    Settings.CLICK_BEHAVIOR_EDIT -> editShortcut(shortcut)
                    Settings.CLICK_BEHAVIOR_MENU -> showContextMenu(shortcutData)
                }
            }
        }
    }

    private fun onItemLongClicked(shortcutData: LiveData<Shortcut?>): Boolean {
        if (tabHost?.isAppLocked() != false || isInMovingMode) {
            return false
        }
        showContextMenu(shortcutData)
        return true
    }

    private fun showContextMenu(shortcutData: LiveData<Shortcut?>) {
        val shortcut = shortcutData.value ?: return
        DialogBuilder(requireContext())
            .title(shortcut.name)
            .item(R.string.action_place) {
                tabHost?.placeShortcutOnHomeScreen(shortcutData.value ?: return@item)
            }
            .item(R.string.action_run) {
                executeShortcut(shortcutData.value ?: return@item)
            }
            .mapIf(isPending(shortcut)) {
                item(R.string.action_cancel_pending) {
                    cancelPendingExecution(shortcutData.value ?: return@item)
                }
            }
            .separator()
            .item(R.string.action_edit) {
                editShortcut(shortcutData.value ?: return@item)
            }
            .mapIf(canMoveShortcuts()) {
                item(R.string.action_move) {
                    openMoveDialog(shortcutData)
                }
            }
            .item(R.string.action_duplicate) {
                duplicateShortcut(shortcutData.value ?: return@item)
            }

            .item(R.string.action_delete) {
                showDeleteDialog(shortcutData)
            }
            .separator()
            .item(R.string.action_shortcut_information) {
                showInfoDialog(shortcutData)
            }
            .item(R.string.action_export) {
                showExportChoiceDialog(shortcutData)
            }
            .showIfPossible()
    }

    private fun isPending(shortcut: Shortcut) =
        pendingShortcuts.any { it.shortcutId == shortcut.id }

    private fun executeShortcut(shortcut: Shortcut) {
        ExecuteActivity.IntentBuilder(requireContext(), shortcut.id)
            .startActivity(this)
    }

    private fun editShortcut(shortcut: Shortcut) {
        ShortcutEditorActivity.IntentBuilder(requireContext())
            .categoryId(categoryId)
            .shortcutId(shortcut.id)
            .startActivity(this, REQUEST_EDIT_SHORTCUT)
    }

    private fun canMoveShortcuts() =
        shortcuts.size > 1 || categories.size > 1

    private fun openMoveDialog(shortcutData: LiveData<Shortcut?>) {
        DialogBuilder(requireContext())
            .mapIf(shortcuts.size > 1) {
                item(R.string.action_enable_moving) {
                    isInMovingMode = true
                }
            }
            .mapIf(categories.size > 1) {
                item(R.string.action_move_to_category) {
                    showMoveToCategoryDialog(shortcutData)
                }
            }
            .showIfPossible()
    }

    private fun showMoveToCategoryDialog(shortcutData: LiveData<Shortcut?>) {
        categoryData.value?.let { currentCategory ->
            DialogBuilder(requireContext())
                .title(R.string.title_move_to_category)
                .mapFor(categories.filter { it.id != currentCategory.id }) { category ->
                    val categoryId = category.id
                    item(name = category.name) {
                        moveShortcut(shortcutData.value ?: return@item, categoryId)
                    }
                }
                .showIfPossible()
        }
    }

    private fun moveShortcut(shortcut: Shortcut, categoryId: String) {
        val name = shortcut.name
        viewModel.moveShortcut(shortcut.id, targetCategoryId = categoryId)
            .subscribe {
                activity?.showSnackbar(String.format(getString(R.string.shortcut_moved), name))
            }
            .attachTo(destroyer)
    }

    private fun duplicateShortcut(shortcut: Shortcut) {
        val name = shortcut.name
        val newName = String.format(getString(R.string.template_shortcut_name_copy), shortcut.name)
        val categoryId = categoryData.value?.id ?: return
        val newPosition = categoryData.value
            ?.shortcuts
            ?.indexOfFirst { it.id == shortcut.id }
            .takeIf { it != -1 }
            ?.let { it + 1 }
        viewModel.duplicateShortcut(shortcut.id, newName, newPosition, categoryId)
            .subscribe {
                showSnackbar(String.format(getString(R.string.shortcut_duplicated), name))
            }
            .attachTo(destroyer)
    }

    private fun cancelPendingExecution(shortcut: Shortcut) {
        viewModel.removePendingExecution(shortcut.id)
            .subscribe {
                showSnackbar(String.format(getString(R.string.pending_shortcut_execution_cancelled), shortcut.name))
                ExecutionScheduler.schedule(requireContext())
            }
            .attachTo(destroyer)
    }

    private fun showExportChoiceDialog(shortcutData: LiveData<Shortcut?>) {
        val shortcut = shortcutData.value ?: return
        if (shortcut.type.usesUrl) {
            DialogBuilder(requireContext())
                .title(R.string.title_export_shortcut_as)
                .item(R.string.action_export_as_curl) {
                    showCurlExportDialog(shortcutData)
                }
                .item(R.string.action_export_as_file) {
                    showFileExportDialog(shortcutData)
                }
                .showIfPossible()
        } else {
            showFileExportDialog(shortcutData)
        }
    }

    private fun showCurlExportDialog(shortcutData: LiveData<Shortcut?>) {
        CurlExporter.generateCommand(requireContext(), shortcutData.value ?: return)
            .subscribe(
                { command ->
                    CurlExportDialog(
                        requireContext(),
                        shortcutData.value?.name ?: return@subscribe,
                        CurlConstructor.toCurlCommandString(command)
                    )
                        .show()
                },
                { e ->
                    if (e !is CanceledByUserException) {
                        activity?.showToast(R.string.error_generic)
                        logException(e)
                    }
                }
            )
            .attachTo(destroyer)
    }

    private fun showFileExportDialog(shortcutData: LiveData<Shortcut?>) {
        val shortcut = shortcutData.value ?: return
        val shortcutId = shortcut.id
        val variableIds = getVariableIdsRequiredForExport(shortcut)
        exportUI.showExportOptions(format = ExportFormat.getPreferredFormat(requireContext()), shortcutId, variableIds) { intent ->
            viewModel.exportedShortcutId = shortcutId
            intent.startActivity(this, REQUEST_EXPORT)
        }
    }

    private fun showDeleteDialog(shortcutData: LiveData<Shortcut?>) {
        DialogBuilder(requireContext())
            .message(R.string.confirm_delete_shortcut_message)
            .positive(R.string.dialog_delete) {
                deleteShortcut(shortcutData.value ?: return@positive)
            }
            .negative(R.string.dialog_cancel)
            .showIfPossible()
    }

    private fun deleteShortcut(shortcut: Shortcut) {
        showSnackbar(String.format(getString(R.string.shortcut_deleted), shortcut.name))
        tabHost?.removeShortcutFromHomeScreen(shortcut)
        viewModel.deleteShortcut(shortcut.id)
            .subscribe {
                ExecutionScheduler.schedule(requireContext())
            }
            .attachTo(destroyer)
    }

    private fun showInfoDialog(shortcutData: LiveData<Shortcut?>) {
        val shortcut = shortcutData.value ?: return
        ShortcutInfoDialog(requireContext(), shortcut)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            REQUEST_EDIT_SHORTCUT -> {
                tabHost?.updateLauncherShortcuts()
            }
            REQUEST_EXPORT -> {
                if (resultCode == RESULT_OK) {
                    startExport(intent?.data ?: return, viewModel.exportedShortcutId!!)
                }
            }
        }
    }

    private fun startExport(uri: Uri, shortcutId: String) {
        val shortcut = shortcuts.value?.find { it.id == shortcutId } ?: return
        val variableIds = getVariableIdsRequiredForExport(shortcut)

        exportUI.startExport(
            uri,
            format = ExportFormat.getPreferredFormat(requireContext()),
            shortcutId = shortcutId,
            variableIds = variableIds,
        )
    }

    private fun getVariableIdsRequiredForExport(shortcut: Shortcut) =
        // TODO: Recursively collect variables referenced by other variables
        RealmFactory.withRealm { realm ->
            VariableResolver.extractVariableIds(
                shortcut,
                variableLookup = VariableManager(Repository.getBase(realm)!!.variables),
            )
        }

    override fun onPause() {
        super.onPause()
        isInMovingMode = false
    }

    override fun onBackPressed(): Boolean {
        if (isInMovingMode) {
            isInMovingMode = false
            return true
        }
        return false
    }

    private val tabHost: TabHost?
        get() = activity as? TabHost

    internal interface TabHost {

        fun selectShortcut(shortcut: Shortcut)

        fun placeShortcutOnHomeScreen(shortcut: Shortcut)

        fun removeShortcutFromHomeScreen(shortcut: Shortcut)

        fun isAppLocked(): Boolean

        fun updateLauncherShortcuts()

        var isInMovingMode: Boolean

    }

    companion object {

        fun create(categoryId: String, selectionMode: SelectionMode): ListFragment =
            ListFragment()
                .apply {
                    arguments = Bundle()
                        .apply {
                            putString(ARG_CATEGORY_ID, categoryId)
                            putSerializable(ARG_SELECTION_MODE, selectionMode)
                        }
                }

        private const val REQUEST_EDIT_SHORTCUT = 2
        private const val REQUEST_EXPORT = 3

        private const val ARG_CATEGORY_ID = "categoryId"
        private const val ARG_SELECTION_MODE = "selectionMode"

    }

}
