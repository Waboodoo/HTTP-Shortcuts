package ch.rmy.android.http_shortcuts.activities.categories

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotterknife.bindView

class CategoriesActivity : BaseActivity() {

    private val viewModel: CategoriesViewModel by bindViewModel()

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

        adapter.clickListener = ::showContextMenu

        initDragOrdering()

        createButton.setOnClickListener { openCreateDialog() }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { categories.size > 1 }
        dragOrderingHelper.attachTo(categoryList)
        dragOrderingHelper.positionChangeSource
            .concatMapCompletable { (oldPosition, newPosition) ->
                val category = categories[oldPosition]!!
                viewModel.moveCategory(category.id, newPosition)
            }
            .subscribe()
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

    private fun showContextMenu(categoryData: LiveData<Category?>) {
        val category = categoryData.value ?: return
        MenuDialogBuilder(context)
            .title(category.name)
            .item(R.string.action_rename) {
                showRenameDialog(categoryData)
            }
            .item(R.string.action_change_category_layout_type) {
                showLayoutTypeDialog(categoryData)
            }
            .item(R.string.action_change_category_background) {
                showBackgroundChangeDialog(categoryData)
            }
            .mapIf(categories.size > 1) {
                it.item(R.string.action_delete) {
                    showDeleteDialog(categoryData)
                }
            }
            .showIfPossible()
    }

    private fun showRenameDialog(categoryData: LiveData<Category?>) {
        val category = categoryData.value ?: return
        MaterialDialog.Builder(context)
            .title(R.string.title_rename_category)
            .inputRange(NAME_MIN_LENGTH, NAME_MAX_LENGTH)
            .input(getString(R.string.placeholder_category_name), category.name) { _, input ->
                renameCategory(categoryData, input.toString())
            }
            .showIfPossible()
    }

    private fun showLayoutTypeDialog(categoryData: LiveData<Category?>) {
        MenuDialogBuilder(context)
            .item(R.string.layout_type_linear_list) {
                changeLayoutType(categoryData, Category.LAYOUT_LINEAR_LIST)
            }
            .item(R.string.layout_type_grid) {
                changeLayoutType(categoryData, Category.LAYOUT_GRID)
            }
            .showIfPossible()
    }

    private fun showBackgroundChangeDialog(categoryData: LiveData<Category?>) {
        MenuDialogBuilder(context)
            .item(R.string.category_background_type_white) {
                changeBackgroundType(categoryData, Category.BACKGROUND_TYPE_WHITE)
            }
            .item(R.string.category_background_type_black) {
                changeBackgroundType(categoryData, Category.BACKGROUND_TYPE_BLACK)
            }
            .item(R.string.category_background_type_wallpaper) {
                PermissionManager.requestFileStoragePermissionIfNeeded(this)
                changeBackgroundType(categoryData, Category.BACKGROUND_TYPE_WALLPAPER)
            }
            .showIfPossible()
    }

    private fun renameCategory(categoryData: LiveData<Category?>, newName: String) {
        val category = categoryData.value ?: return
        viewModel.renameCategory(category.id, newName)
            .subscribe {
                showSnackbar(R.string.message_category_renamed)
            }
            .attachTo(destroyer)
    }

    private fun changeLayoutType(categoryData: LiveData<Category?>, layoutType: String) {
        val category = categoryData.value ?: return
        viewModel.setLayoutType(category.id, layoutType)
            .subscribe {
                showSnackbar(R.string.message_layout_type_changed)
            }
            .attachTo(destroyer)
    }

    private fun changeBackgroundType(categoryData: LiveData<Category?>, backgroundType: String) {
        val category = categoryData.value ?: return
        viewModel.setBackground(category.id, backgroundType)
            .subscribe {
                showSnackbar(R.string.message_background_type_changed)
            }
            .attachTo(destroyer)
    }

    private fun showDeleteDialog(categoryData: LiveData<Category?>) {
        val category = categoryData.value ?: return
        if (category.shortcuts.isEmpty()) {
            deleteCategory(category)
            return
        }
        MaterialDialog.Builder(context)
            .content(R.string.confirm_delete_category_message)
            .positiveText(R.string.dialog_delete)
            .onPositive { _, _ -> deleteCategory(categoryData.value ?: return@onPositive) }
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
