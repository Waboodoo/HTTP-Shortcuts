package ch.rmy.android.http_shortcuts.activities.main

import android.app.WallpaperManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.CurlExportDialog
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.http.ExecutionScheduler
import ch.rmy.android.http_shortcuts.utils.GridLayoutManager
import ch.rmy.android.http_shortcuts.utils.SelectionMode
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.curlcommand.CurlCommand
import ch.rmy.curlcommand.CurlConstructor
import kotterknife.bindView
import org.apache.http.HttpHeaders
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class ListFragment : BaseFragment() {

    val categoryId by lazy {
        args.getString(ARG_CATEGORY_ID) ?: ""
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

        categoryData.value?.background?.let {
            updateBackground(it)
            adapter?.textColor = if (it == Category.BACKGROUND_TYPE_WHITE) {
                BaseShortcutAdapter.TextColor.DARK
            } else {
                BaseShortcutAdapter.TextColor.BRIGHT
            }
        }
    }

    private fun updateBackground(background: String) {
        backgroundView.apply {
            when (background) {
                Category.BACKGROUND_TYPE_BLACK -> {
                    setImageDrawable(null)
                    setBackgroundColor(color(context!!, R.color.activity_background_dark))
                }
                Category.BACKGROUND_TYPE_WALLPAPER -> {
                    wallpaper
                        ?.also {
                            setImageDrawable(it)
                        }
                        ?: run {
                            setImageDrawable(null)
                            setBackgroundColor(color(context!!, R.color.activity_background))
                        }
                }
                else -> {
                    setImageDrawable(null)
                    setBackgroundColor(color(context!!, R.color.activity_background))
                }
            }
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
                when (Settings(context!!).clickBehavior) {
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
        DialogBuilder(context!!)
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
        ExecuteActivity.IntentBuilder(context!!, shortcut.id)
            .build()
            .startActivity(this)
    }

    private fun editShortcut(shortcut: Shortcut) {
        ShortcutEditorActivity.IntentBuilder(context!!)
            .categoryId(categoryId)
            .shortcutId(shortcut.id)
            .build()
            .startActivity(this, REQUEST_EDIT_SHORTCUT)
    }

    private fun canMoveShortcut(shortcut: Shortcut): Boolean =
        canMoveShortcut(shortcut, -1) || canMoveShortcut(shortcut, +1) || categories.size > 1

    private fun canMoveShortcut(shortcut: Shortcut, offset: Int): Boolean {
        val position = shortcuts.indexOf(shortcut) + offset
        return position >= 0 && position < shortcuts.size
    }

    private fun openMoveDialog(shortcutData: LiveData<Shortcut?>) {
        val shortcut = shortcutData.value ?: return
        DialogBuilder(context!!)
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
            DialogBuilder(context!!)
                .title(R.string.title_move_to_category)
                .mapFor(categories.filter { it.id != currentCategory.id }) { builder, category ->
                    val categoryId = category.id
                    builder.item(category.name) {
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
        val newName = String.format(getString(R.string.copy), shortcut.name)
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
            .mapIf(shortcut.usesCustomBody()) { builder ->
                builder.header(HttpHeaders.CONTENT_TYPE, shortcut.contentType.ifEmpty { Shortcut.DEFAULT_CONTENT_TYPE })
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
            shortcut.name,
            CurlConstructor.toCurlCommandString(command)
        ).show()
    }

    private fun showDeleteDialog(shortcutData: LiveData<Shortcut?>) {
        DialogBuilder(context!!)
            .message(R.string.confirm_delete_shortcut_message)
            .positive(R.string.dialog_delete) { deleteShortcut(shortcutData.value ?: return@positive) }
            .negative(R.string.dialog_cancel)
            .showIfPossible()
    }

    private fun deleteShortcut(shortcut: Shortcut) {
        showSnackbar(String.format(getString(R.string.shortcut_deleted), shortcut.name))
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

        fun isAppLocked(): Boolean

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

        private const val ARG_CATEGORY_ID = "categoryId"
        private const val ARG_SELECTION_MODE = "selectionMode"

    }

}
