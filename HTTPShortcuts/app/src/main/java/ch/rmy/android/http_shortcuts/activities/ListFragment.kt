package ch.rmy.android.http_shortcuts.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.adapters.ShortcutAdapter
import ch.rmy.android.http_shortcuts.adapters.ShortcutGridAdapter
import ch.rmy.android.http_shortcuts.adapters.ShortcutListAdapter
import ch.rmy.android.http_shortcuts.dialogs.CurlExportDialog
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.*
import ch.rmy.curlcommand.CurlCommand
import ch.rmy.curlcommand.CurlConstructor
import com.afollestad.materialdialogs.MaterialDialog
import io.realm.RealmChangeListener
import io.realm.RealmList
import kotterknife.bindView
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class ListFragment : BaseFragment() {

    override val layoutResource = R.layout.fragment_list

    internal val shortcutList: RecyclerView by bindView(R.id.shortcut_list)

    var categoryId: Long = 0
        set(categoryId) {
            field = categoryId
            onCategoryChanged()
        }
    private var selectionMode: SelectionMode? = null
    private var category: Category? = null

    private var controller: Controller? = null
    private var categories: List<Category>? = null

    private var listDivider: RecyclerView.ItemDecoration? = null

    private val shortcutChangeListener = RealmChangeListener<RealmList<Shortcut>> { shortcuts -> onShortcutsChanged(shortcuts) }

    private val clickListener = object : OnItemClickedListener<Shortcut> {
        override fun onItemClicked(item: Shortcut) {
            when (selectionMode) {
                SelectionMode.HOME_SCREEN, SelectionMode.PLUGIN -> tabHost.selectShortcut(item)
                else -> {
                    val action = Settings(context).clickBehavior
                    when (action) {
                        Settings.CLICK_BEHAVIOR_RUN -> executeShortcut(item)
                        Settings.CLICK_BEHAVIOR_EDIT -> editShortcut(item)
                        Settings.CLICK_BEHAVIOR_MENU -> showContextMenu(item)
                    }
                }
            }
        }

        override fun onItemLongClicked(item: Shortcut) {
            showContextMenu(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        controller = Controller()
        categories = controller!!.categories

        listDivider = ShortcutListDecorator(context, R.drawable.list_divider)
    }

    private fun onCategoryChanged() {
        if (controller == null) {
            return
        }
        if (category != null) {
            category!!.shortcuts!!.removeChangeListener(shortcutChangeListener)
        }
        category = controller!!.getCategoryById(this.categoryId)
        if (category == null) {
            return
        }
        category!!.shortcuts!!.addChangeListener(shortcutChangeListener)

        val manager: RecyclerView.LayoutManager
        val adapter: ShortcutAdapter
        when (category!!.layoutType) {
            Category.LAYOUT_GRID -> {
                adapter = ShortcutGridAdapter(context)
                manager = GridLayoutManager(context)
                shortcutList.removeItemDecoration(listDivider)
            }
            else -> {
                adapter = ShortcutListAdapter(context)
                manager = LinearLayoutManager(context)
                shortcutList.addItemDecoration(listDivider)
            }
        }
        adapter.setPendingShortcuts(controller!!.shortcutsPendingExecution)
        adapter.clickListener = clickListener
        adapter.setItems(category!!.shortcuts!!)

        shortcutList.layoutManager = manager
        shortcutList.adapter = adapter
        onShortcutsChanged(category!!.shortcuts!!)
    }

    private fun onShortcutsChanged(shortcuts: List<Shortcut>) {
        if (shortcutList.layoutManager is GridLayoutManager) {
            (shortcutList.layoutManager as GridLayoutManager).setEmpty(shortcuts.isEmpty())
        }
        LauncherShortcutManager.updateAppShortcuts(context, controller!!.categories)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (category != null) {
            category!!.shortcuts!!.removeAllChangeListeners()
        }
        controller!!.destroy()
    }

    override fun setupViews(parent: View) {
        shortcutList.setHasFixedSize(true)
        onCategoryChanged()
    }

    fun setSelectionMode(selectionMode: SelectionMode) {
        this.selectionMode = selectionMode
    }

    private fun showContextMenu(shortcut: Shortcut) {
        val builder = MenuDialogBuilder(context)
                .title(shortcut.name!!)
                .item(R.string.action_place, {
                    tabHost.placeShortcutOnHomeScreen(shortcut)
                })
                .item(R.string.action_run, {
                    executeShortcut(shortcut)
                })
                .item(R.string.action_edit, {
                    editShortcut(shortcut)
                })
        if (canMoveShortcut(shortcut)) {
            builder.item(R.string.action_move, {
                openMoveDialog(shortcut)
            })
        }
        builder.item(R.string.action_duplicate, {
            duplicateShortcut(shortcut)
        })
        if (getPendingExecution(shortcut) != null) {
            builder.item(R.string.action_cancel_pending, {
                cancelPendingExecution(shortcut)
            })
        }
        builder
                .item(R.string.action_curl_export, {
                    showCurlExportDialog(shortcut)
                })
                .item(R.string.action_delete, {
                    showDeleteDialog(shortcut)
                })
                .show()
    }

    private fun getPendingExecution(shortcut: Shortcut): PendingExecution? {
        return controller!!.shortcutsPendingExecution.firstOrNull { it.shortcutId == shortcut.id }
    }

    private fun executeShortcut(shortcut: Shortcut) {
        val intent = IntentUtil.createIntent(context, shortcut.id)
        startActivity(intent)
    }

    private fun editShortcut(shortcut: Shortcut) {
        val intent = Intent(context, EditorActivity::class.java)
        intent.putExtra(EditorActivity.EXTRA_SHORTCUT_ID, shortcut.id)
        startActivityForResult(intent, REQUEST_EDIT_SHORTCUT)
    }

    private fun canMoveShortcut(shortcut: Shortcut): Boolean {
        return canMoveShortcut(shortcut, -1) || canMoveShortcut(shortcut, +1) || categories!!.size > 1
    }

    private fun canMoveShortcut(shortcut: Shortcut, offset: Int): Boolean {
        if (category == null) {
            return false
        }
        val position = category!!.shortcuts!!.indexOf(shortcut) + offset
        return position >= 0 && position < category!!.shortcuts!!.size
    }

    private fun openMoveDialog(shortcut: Shortcut) {
        val builder = MenuDialogBuilder(context)
        if (canMoveShortcut(shortcut, -1)) {
            builder.item(R.string.action_move_up, {
                moveShortcut(shortcut, -1)
            })
        }
        if (canMoveShortcut(shortcut, 1)) {
            builder.item(R.string.action_move_down, {
                moveShortcut(shortcut, 1)
            })
        }
        if (categories!!.size > 1) {
            builder.item(R.string.action_move_to_category, {
                showMoveToCategoryDialog(shortcut)
            })
        }
        builder.show()
    }

    private fun moveShortcut(shortcut: Shortcut, offset: Int) {
        if (!canMoveShortcut(shortcut, offset) || category == null) {
            return
        }
        val position = category!!.shortcuts!!.indexOf(shortcut) + offset
        if (position == category!!.shortcuts!!.size) {
            controller!!.moveShortcut(shortcut, category!!)
        } else {
            controller!!.moveShortcut(shortcut, position)
        }
    }

    private fun showMoveToCategoryDialog(shortcut: Shortcut) {
        if (this.category == null) {
            return
        }
        val categoryNames = ArrayList<CharSequence>()
        val categories = ArrayList<Category>()
        for (category in this.categories!!) {
            if (category.id != this.category!!.id) {
                categoryNames.add(category.name!!)
                categories.add(category)
            }
        }

        MaterialDialog.Builder(context)
                .title(R.string.title_move_to_category)
                .items(categoryNames)
                .itemsCallback { _, _, which, _ ->
                    if (which < categories.size && categories[which].isValid) {
                        moveShortcut(shortcut, categories[which])
                    }
                }
                .show()
    }

    private fun moveShortcut(shortcut: Shortcut, category: Category) {
        controller!!.moveShortcut(shortcut, category)
        tabHost.showSnackbar(String.format(getString(R.string.shortcut_moved), shortcut.name))
    }

    private fun duplicateShortcut(shortcut: Shortcut) {
        if (category == null) {
            return
        }
        val newName = String.format(getString(R.string.copy), shortcut.name)
        val duplicate = controller!!.persist(shortcut.duplicate(newName))
        controller!!.moveShortcut(duplicate, category!!)

        var position = category!!.shortcuts!!.size
        var i = 0
        for (s in category!!.shortcuts!!) {
            if (s.id == shortcut.id) {
                position = i + 1
                break
            }
            i++
        }
        controller!!.moveShortcut(duplicate, position)

        tabHost.showSnackbar(String.format(getString(R.string.shortcut_duplicated), shortcut.name))
    }

    private fun cancelPendingExecution(shortcut: Shortcut) {
        val pendingExecution = getPendingExecution(shortcut) ?: return
        controller!!.removePendingExecution(pendingExecution)
        tabHost.showSnackbar(String.format(getString(R.string.pending_shortcut_execution_cancelled), shortcut.name))
    }

    private fun showCurlExportDialog(shortcut: Shortcut) {
        val builder = CurlCommand.Builder()
                .url(shortcut.url)
                .username(shortcut.username)
                .password(shortcut.password)
                .method(shortcut.method)
                .timeout(shortcut.timeout)

        for (header in shortcut.headers!!) {
            builder.header(header.key, header.value)
        }

        for (parameter in shortcut.parameters!!) {
            try {
                builder.data(URLEncoder.encode(parameter.key, "UTF-8")
                        + "="
                        + URLEncoder.encode(parameter.value, "UTF-8")
                        + "&"
                )
            } catch (e: UnsupportedEncodingException) {

            }

        }

        builder.data(shortcut.bodyContent)

        CurlExportDialog(
                context,
                shortcut.getSafeName(context),
                CurlConstructor.toCurlCommandString(builder.build())
        ).show()
    }

    private fun showDeleteDialog(shortcut: Shortcut) {
        MaterialDialog.Builder(context)
                .content(R.string.confirm_delete_shortcut_message)
                .positiveText(R.string.dialog_delete)
                .onPositive { _, _ -> deleteShortcut(shortcut) }
                .negativeText(R.string.dialog_cancel)
                .show()
    }

    private fun deleteShortcut(shortcut: Shortcut) {
        tabHost.showSnackbar(String.format(getString(R.string.shortcut_deleted), shortcut.name))
        tabHost.removeShortcutFromHomeScreen(shortcut)
        controller!!.deleteShortcut(shortcut)
    }

    private val tabHost: TabHost
        get() = activity as TabHost

    internal interface TabHost {

        fun selectShortcut(shortcut: Shortcut)

        fun placeShortcutOnHomeScreen(shortcut: Shortcut)

        fun removeShortcutFromHomeScreen(shortcut: Shortcut)

        fun showSnackbar(message: CharSequence)

    }

    companion object {

        private const val REQUEST_EDIT_SHORTCUT = 2
    }

}
