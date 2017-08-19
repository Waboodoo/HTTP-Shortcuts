package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.adapters.CategoryAdapter
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.utils.ShortcutListDecorator
import com.afollestad.materialdialogs.MaterialDialog
import io.realm.RealmList
import kotterknife.bindView

class CategoriesActivity : BaseActivity() {

    internal val categoryList: RecyclerView by bindView(R.id.category_list)
    internal val createButton: FloatingActionButton by bindView(R.id.button_create_category)

    private var controller: Controller? = null
    private var categories: RealmList<Category>? = null

    private val clickedListener = object : OnItemClickedListener<Category> {
        override fun onItemClicked(item: Category) {
            showContextMenu(item)
        }

        override fun onItemLongClicked(item: Category) {
            showContextMenu(item)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        controller = destroyer.own(Controller())
        categories = controller!!.categories
        val adapter = destroyer.own(CategoryAdapter(this))
        adapter.setItems(controller!!.base.categories!!)

        val manager = LinearLayoutManager(this)
        categoryList.layoutManager = manager
        categoryList.setHasFixedSize(true)
        categoryList.addItemDecoration(ShortcutListDecorator(this, R.drawable.list_divider))
        categoryList.adapter = adapter

        adapter.clickListener = clickedListener

        createButton.setOnClickListener { openCreateDialog() }
    }

    private fun openCreateDialog() {
        MaterialDialog.Builder(this)
                .title(R.string.title_create_category)
                .inputRange(NAME_MIN_LENGTH, NAME_MAX_LENGTH)
                .input(getString(R.string.placeholder_category_name), null) { _, input -> createCategory(input.toString()) }.show()
    }

    private fun createCategory(name: String) {
        controller!!.createCategory(name)
        showSnackbar(R.string.message_category_created)
    }

    private fun showContextMenu(category: Category) {
        val builder = MenuDialogBuilder(this)
                .title(category.name!!)
                .item(R.string.action_rename, {
                    showRenameDialog(category)
                }).item(R.string.action_change_category_layout_type, {
            showLayoutTypeDialog(category)
        })
        if (canMoveCategory(category, -1)) {
            builder.item(R.string.action_move_up, {
                moveCategory(category, -1)
            })
        }
        if (canMoveCategory(category, 1)) {
            builder.item(R.string.action_move_down, {
                moveCategory(category, 1)
            })
        }
        if (categories!!.size > 1) {
            builder.item(R.string.action_delete, {
                showDeleteDialog(category)
            })
        }

        builder.show()
    }

    private fun showRenameDialog(category: Category) {
        MaterialDialog.Builder(this)
                .title(R.string.title_rename_category)
                .inputRange(NAME_MIN_LENGTH, NAME_MAX_LENGTH)
                .input(getString(R.string.placeholder_category_name), category.name) { _, input -> renameCategory(category, input.toString()) }.show()
    }

    private fun showLayoutTypeDialog(category: Category) {
        MenuDialogBuilder(this)
                .item(R.string.layout_type_linear_list, {
                    changeLayoutType(category, Category.LAYOUT_LINEAR_LIST)
                })
                .item(R.string.layout_type_grid, {
                    changeLayoutType(category, Category.LAYOUT_GRID)
                })
                .show()
    }

    private fun renameCategory(category: Category, newName: String) {
        controller!!.renameCategory(category, newName)
        showSnackbar(R.string.message_category_renamed)
    }

    private fun changeLayoutType(category: Category, layoutType: String) {
        controller!!.setLayoutType(category, layoutType)
        showSnackbar(R.string.message_layout_type_changed)
    }

    private fun canMoveCategory(category: Category, offset: Int): Boolean {
        val position = categories!!.indexOf(category) + offset
        return position >= 0 && position < categories!!.size
    }

    private fun moveCategory(category: Category, offset: Int) {
        if (!canMoveCategory(category, offset)) {
            return
        }
        val position = categories!!.indexOf(category) + offset
        controller!!.moveCategory(category, position)
    }

    private fun showDeleteDialog(category: Category) {
        if (category.shortcuts!!.isEmpty()) {
            deleteCategory(category)
            return
        }
        MaterialDialog.Builder(this)
                .content(R.string.confirm_delete_category_message)
                .positiveText(R.string.dialog_delete)
                .onPositive { _, _ -> deleteCategory(category) }
                .negativeText(R.string.dialog_cancel)
                .show()
    }

    private fun deleteCategory(category: Category) {
        controller!!.deleteCategory(category)
        showSnackbar(R.string.message_category_deleted)
    }

    companion object {

        private const val NAME_MIN_LENGTH = 1
        private const val NAME_MAX_LENGTH = 20
    }

}
