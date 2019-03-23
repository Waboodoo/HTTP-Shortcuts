package ch.rmy.android.http_shortcuts.activities.main

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.activities.EditorActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.adapters.ShortcutAdapter
import ch.rmy.android.http_shortcuts.adapters.ShortcutGridAdapter
import ch.rmy.android.http_shortcuts.adapters.ShortcutListAdapter
import ch.rmy.android.http_shortcuts.dialogs.CurlExportDialog
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.http.ExecutionScheduler
import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.GridLayoutManager
import ch.rmy.android.http_shortcuts.utils.SelectionMode
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.curlcommand.CurlCommand
import ch.rmy.curlcommand.CurlConstructor
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class ListFragment : BaseFragment() {

    val categoryId by lazy {
        args.getLong(ARG_CATEGORY_ID)
    }

    private val selectionMode by lazy {
        args.getSerializable(ARG_SELECTION_MODE) as SelectionMode
    }

    private val viewModel: ShortcutListViewModel by bindViewModel()

    private lateinit var categories: ListLiveData<Category>
    private lateinit var categoryData: LiveData<Category?>
    private lateinit var shortcuts: ListLiveData<Shortcut>
    private lateinit var pendingShortcuts: ListLiveData<PendingExecution>

    private var layoutType: String? = null
    private var adapter: ShortcutAdapter? = null

    override val layoutResource = R.layout.fragment_list

    // Views
    private val shortcutList: RecyclerView by bindView(R.id.shortcut_list)

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
        categoryData.observe(this, Observer {
            updateViews()
        })
        pendingShortcuts.observe(this, Observer { pendingShortcuts ->
            adapter?.setPendingShortcuts(pendingShortcuts)
        })
        shortcuts.observe(this, Observer {
            updateEmptyState()
        })
    }

    private fun updateViews() {
        val layoutType = categoryData.value?.layoutType

        if (layoutType != this.layoutType || adapter == null) {
            this.layoutType = layoutType
            adapter?.destroy()

            val adapter = when (layoutType) {
                Category.LAYOUT_GRID -> ShortcutGridAdapter(context!!, shortcuts)
                else -> ShortcutListAdapter(context!!, shortcuts)
            }
            val manager = when (layoutType) {
                Category.LAYOUT_GRID -> GridLayoutManager(context!!)
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
    }

    private fun updateEmptyState() {
        (shortcutList.layoutManager as? GridLayoutManager)?.setEmpty(shortcuts.isEmpty())
    }

    private fun onItemClicked(shortcutData: LiveData<Shortcut?>) {
        val shortcut = shortcutData.value ?: return
        when (selectionMode) {
            SelectionMode.HOME_SCREEN, SelectionMode.PLUGIN -> tabHost?.selectShortcut(shortcut)
            else -> {
                if (tabHost?.isAppLocked() == true) {
                    executeShortcut(shortcut)
                    return
                }
                val action = Settings(context!!).clickBehavior
                when (action) {
                    Settings.CLICK_BEHAVIOR_RUN -> executeShortcut(shortcut)
                    Settings.CLICK_BEHAVIOR_EDIT -> editShortcut(shortcut)
                    Settings.CLICK_BEHAVIOR_MENU -> showContextMenu(shortcutData)
                }
            }
        }
    }

    private fun onItemLongClicked(shortcutData: LiveData<Shortcut?>): Boolean {
        if (tabHost?.isAppLocked() != false) {
            return false
        }
        showContextMenu(shortcutData)
        return true
    }

    private fun showContextMenu(shortcutData: LiveData<Shortcut?>) {
        val shortcut = shortcutData.value ?: return
        MenuDialogBuilder(context!!)
            .title(shortcut.name)
            .item(R.string.action_place) {
                tabHost?.placeShortcutOnHomeScreen(shortcutData.value ?: return@item)
            }
            .item(R.string.action_run) {
                executeShortcut(shortcutData.value ?: return@item)
            }
            .item(R.string.action_edit) {
                editShortcut(shortcutData.value ?: return@item)
            }
            .mapIf(canMoveShortcut(shortcut)) {
                it.item(R.string.action_move) {
                    openMoveDialog(shortcutData)
                }
            }
            .item(R.string.action_duplicate) {
                duplicateShortcut(shortcutData.value ?: return@item)
            }
            .mapIf(isPending(shortcut)) {
                it.item(R.string.action_cancel_pending) {
                    cancelPendingExecution(shortcutData.value ?: return@item)
                }
            }
            .item(R.string.action_curl_export) {
                showCurlExportDialog(shortcutData.value ?: return@item)
            }
            .item(R.string.action_delete) {
                showDeleteDialog(shortcutData)
            }
            .showIfPossible()
    }

    private fun isPending(shortcut: Shortcut) =
        pendingShortcuts.any { it.shortcutId == shortcut.id }

    private fun executeShortcut(shortcut: Shortcut) {
        val intent = ExecuteActivity.IntentBuilder(context!!, shortcut.id)
            .build()
        startActivity(intent)
    }

    private fun editShortcut(shortcut: Shortcut) {
        val intent = EditorActivity.IntentBuilder(context!!)
            .shortcutId(shortcut.id)
            .build()
        startActivityForResult(intent, REQUEST_EDIT_SHORTCUT)
    }

    private fun canMoveShortcut(shortcut: Shortcut): Boolean =
        canMoveShortcut(shortcut, -1) || canMoveShortcut(shortcut, +1) || categories.size > 1

    private fun canMoveShortcut(shortcut: Shortcut, offset: Int): Boolean {
        val position = shortcuts.indexOf(shortcut) + offset
        return position >= 0 && position < shortcuts.size
    }

    private fun openMoveDialog(shortcutData: LiveData<Shortcut?>) {
        val shortcut = shortcutData.value ?: return
        MenuDialogBuilder(context!!)
            .mapIf(canMoveShortcut(shortcut, -1)) {
                it.item(R.string.action_move_up) {
                    moveShortcut(shortcut, -1)
                }
            }
            .mapIf(canMoveShortcut(shortcut, 1)) {
                it.item(R.string.action_move_down) {
                    moveShortcut(shortcut, 1)
                }
            }
            .mapIf(categories.size > 1) {
                it.item(R.string.action_move_to_category) {
                    showMoveToCategoryDialog(shortcutData)
                }
            }
            .showIfPossible()
    }

    private fun moveShortcut(shortcut: Shortcut, offset: Int) {
        categoryData.value?.let { currentCategory ->
            if (!canMoveShortcut(shortcut, offset)) {
                return
            }
            val position = currentCategory.shortcuts.indexOf(shortcut) + offset
            if (position == currentCategory.shortcuts.size) {
                viewModel.moveShortcut(shortcut.id, targetCategoryId = currentCategory.id)
            } else {
                viewModel.moveShortcut(shortcut.id, targetPosition = position)
            }
                .subscribe()
                .attachTo(destroyer)
        }
    }

    private fun showMoveToCategoryDialog(shortcutData: LiveData<Shortcut?>) {
        categoryData.value?.let { currentCategory ->
            MenuDialogBuilder(context!!)
                .title(R.string.title_move_to_category)
                .mapFor(categories.filter { it.id != currentCategory.id }) { builder, category ->
                    builder.item(category.name) {
                        categoryData.value?.let { category ->
                            moveShortcut(shortcutData.value ?: return@item, category)
                        }
                    }
                }
                .showIfPossible()
        }
    }

    private fun moveShortcut(shortcut: Shortcut, category: Category) {
        val name = shortcut.name
        viewModel.moveShortcut(shortcut.id, targetCategoryId = category.id)
            .subscribe {
                tabHost?.showSnackbar(String.format(getString(R.string.shortcut_moved), name))
            }
            .attachTo(destroyer)
    }

    private fun duplicateShortcut(shortcut: Shortcut) {
        val name = shortcut.name
        val newName = String.format(getString(R.string.copy), shortcut.name)
        val categoryId = categoryData.value?.id ?: return
        val newPosition = categoryData.value
            ?.shortcuts
            ?.indexOfFirst { it.id == shortcut.id }
            .takeIf { it != -1 }
            ?.let { it + 1 }
        viewModel.duplicateShortcut(shortcut.id, newName, newPosition, categoryId)
            .subscribe {
                tabHost?.showSnackbar(String.format(getString(R.string.shortcut_duplicated), name))
            }
            .attachTo(destroyer)
    }

    private fun cancelPendingExecution(shortcut: Shortcut) {
        viewModel.removePendingExecution(shortcut.id)
            .subscribe {
                tabHost?.showSnackbar(String.format(getString(R.string.pending_shortcut_execution_cancelled), shortcut.name))
                ExecutionScheduler.schedule(context!!)
            }
            .attachTo(destroyer)
    }

    private fun showCurlExportDialog(shortcut: Shortcut) {
        val command = CurlCommand.Builder()
            .url(shortcut.url)
            .username(shortcut.username)
            .password(shortcut.password)
            .method(shortcut.method)
            .timeout(shortcut.timeout)
            .mapFor(shortcut.headers) { builder, header ->
                builder.header(header.key, header.value)
            }
            .mapFor(shortcut.parameters) { builder, parameter ->
                try {
                    builder.data(URLEncoder.encode(parameter.key, "UTF-8")
                        + "="
                        + URLEncoder.encode(parameter.value, "UTF-8")
                        + "&"
                    )
                } catch (e: UnsupportedEncodingException) {
                    builder
                }
            }
            .data(shortcut.bodyContent)
            .build()

        CurlExportDialog(
            context!!,
            shortcut.getSafeName(context!!),
            CurlConstructor.toCurlCommandString(command)
        ).show()
    }

    private fun showDeleteDialog(shortcutData: LiveData<Shortcut?>) {
        MaterialDialog.Builder(context!!)
            .content(R.string.confirm_delete_shortcut_message)
            .positiveText(R.string.dialog_delete)
            .onPositive { _, _ -> deleteShortcut(shortcutData.value ?: return@onPositive) }
            .negativeText(R.string.dialog_cancel)
            .showIfPossible()
    }

    private fun deleteShortcut(shortcut: Shortcut) {
        tabHost?.showSnackbar(String.format(getString(R.string.shortcut_deleted), shortcut.name))
        tabHost?.removeShortcutFromHomeScreen(shortcut)
        viewModel.deleteShortcut(shortcut.id)
            .subscribe {
                ExecutionScheduler.schedule(context!!)
            }
            .attachTo(destroyer)
    }

    private val tabHost: TabHost?
        get() = activity as? TabHost

    internal interface TabHost {

        fun selectShortcut(shortcut: Shortcut)

        fun placeShortcutOnHomeScreen(shortcut: Shortcut)

        fun removeShortcutFromHomeScreen(shortcut: Shortcut)

        fun showSnackbar(message: CharSequence)

        fun isAppLocked(): Boolean

    }

    companion object {

        fun create(categoryId: Long, selectionMode: SelectionMode): ListFragment =
            ListFragment()
                .apply {
                    arguments = Bundle()
                        .apply {
                            putLong(ARG_CATEGORY_ID, categoryId)
                            putSerializable(ARG_SELECTION_MODE, selectionMode)
                        }
                }

        private const val REQUEST_EDIT_SHORTCUT = 2

        private const val ARG_CATEGORY_ID = "categoryId"
        private const val ARG_SELECTION_MODE = "selectionMode"

    }

}
