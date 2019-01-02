package ch.rmy.android.http_shortcuts.activities.categories

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.adapters.CategoryAdapter
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.utils.attachTo
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotterknife.bindView

class CategoriesActivity : BaseActivity() {

    private val viewModel: CategoriesViewModel by lazy {
        ViewModelProviders.of(this).get(CategoriesViewModel::class.java)
    }

    // Views
    private val categoryList: RecyclerView by bindView(R.id.category_list)
    private val createButton: FloatingActionButton by bindView(R.id.button_create_category)

    private val categories by lazy { viewModel.getCategories() }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)
        initViews()
    }

    private fun initViews() {
        val adapter = destroyer.own(CategoryAdapter(context, categories))

        val manager = LinearLayoutManager(context)
        categoryList.layoutManager = manager
        categoryList.setHasFixedSize(true)
        categoryList.adapter = adapter

        adapter.clickListener = this::showContextMenu

        initDragOrdering()

        createButton.setOnClickListener { openCreateDialog() }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper()
        dragOrderingHelper.attachTo(categoryList)
        dragOrderingHelper.positionChangeSource.add { (oldPosition, newPosition) ->
            val category = categories[oldPosition]!!
            viewModel.moveCategory(category.id, newPosition)
                .subscribe()
                .attachTo(destroyer)
        }
            .attachTo(destroyer)
    }

    private fun openCreateDialog() {
        MaterialDialog.Builder(context)
            .title(R.string.title_create_category)
            .inputRange(NAME_MIN_LENGTH, NAME_MAX_LENGTH)
            .input(getString(R.string.placeholder_category_name), null) { _, input ->
                createCategory(input.toString())
            }
            .showIfPossible()
    }

    private fun createCategory(name: String) {
        viewModel.createCategory(name)
            .subscribe {
                showSnackbar(R.string.message_category_created)
            }
            .attachTo(destroyer)
    }

    private fun showContextMenu(category: Category) {
        MenuDialogBuilder(context)
            .title(category.name)
            .item(R.string.action_rename) {
                showRenameDialog(category)
            }
            .item(R.string.action_change_category_layout_type) {
                showLayoutTypeDialog(category)
            }
            .mapIf(categories.size > 1) {
                it.item(R.string.action_delete) {
                    showDeleteDialog(category)
                }
            }
            .showIfPossible()
    }

    private fun showRenameDialog(category: Category) {
        MaterialDialog.Builder(context)
            .title(R.string.title_rename_category)
            .inputRange(NAME_MIN_LENGTH, NAME_MAX_LENGTH)
            .input(getString(R.string.placeholder_category_name), category.name) { _, input ->
                renameCategory(category, input.toString())
            }
            .showIfPossible()
    }

    private fun showLayoutTypeDialog(category: Category) {
        MenuDialogBuilder(context)
            .item(R.string.layout_type_linear_list) {
                changeLayoutType(category, Category.LAYOUT_LINEAR_LIST)
            }
            .item(R.string.layout_type_grid) {
                changeLayoutType(category, Category.LAYOUT_GRID)
            }
            .showIfPossible()
    }

    private fun renameCategory(category: Category, newName: String) {
        viewModel.renameCategory(category.id, newName)
            .subscribe {
                showSnackbar(R.string.message_category_renamed)
            }
            .attachTo(destroyer)
    }

    private fun changeLayoutType(category: Category, layoutType: String) {
        viewModel.setLayoutType(category.id, layoutType)
            .subscribe {
                showSnackbar(R.string.message_layout_type_changed)
            }
            .attachTo(destroyer)
    }

    private fun showDeleteDialog(category: Category) {
        if (category.shortcuts.isEmpty()) {
            deleteCategory(category)
            return
        }
        MaterialDialog.Builder(context)
            .content(R.string.confirm_delete_category_message)
            .positiveText(R.string.dialog_delete)
            .onPositive { _, _ -> deleteCategory(category) }
            .negativeText(R.string.dialog_cancel)
            .showIfPossible()
    }

    private fun deleteCategory(category: Category) {
        viewModel.deleteCategory(category.id)
            .subscribe {
                showSnackbar(R.string.message_category_deleted)
            }
            .attachTo(destroyer)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, CategoriesActivity::class.java)

    companion object {

        private const val NAME_MIN_LENGTH = 1
        private const val NAME_MAX_LENGTH = 20

    }
}
