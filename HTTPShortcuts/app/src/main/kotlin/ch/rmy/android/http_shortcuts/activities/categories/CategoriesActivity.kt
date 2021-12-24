package ch.rmy.android.http_shortcuts.activities.categories

import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.databinding.ActivityCategoriesBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.utils.PermissionManager

class CategoriesActivity : BaseActivity() {

    private val viewModel: CategoriesViewModel by bindViewModel()

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var adapter: CategoryAdapter
    private var isDraggingEnabled = false

    override fun onCreate() {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityCategoriesBinding.inflate(layoutInflater))
        setTitle(R.string.title_categories)

        adapter = CategoryAdapter()

        val manager = LinearLayoutManager(context)
        binding.categoryList.layoutManager = manager
        binding.categoryList.setHasFixedSize(true)
        binding.categoryList.adapter = adapter

        binding.buttonCreateCategory.applyTheme(themeHelper)
    }

    private fun initUserInputBindings() {
        initDragOrdering()

        adapter.userEvents.observe(this) { event ->
            when (event) {
                is CategoryAdapter.UserEvent.CategoryClicked -> {
                    viewModel.onCategoryClicked(event.id)
                }
            }
        }

        binding.buttonCreateCategory.setOnClickListener {
            viewModel.onCreateCategoryButtonClicked()
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { isDraggingEnabled }
        dragOrderingHelper.attachTo(binding.categoryList)
        dragOrderingHelper.positionChangeSource.observe(this) { (oldPosition, newPosition) ->
            viewModel.onCategoryMoved(oldPosition, newPosition)
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            adapter.items = viewState.categories
            isDraggingEnabled = viewState.isDraggingEnabled
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is CategoriesEvent.ShowCreateCategoryDialog -> {
                showCreateDialog()
            }
            is CategoriesEvent.ShowContextMenu -> {
                showContextMenu(event)
            }
            is CategoriesEvent.ShowRenameDialog -> {
                showRenameDialog(event)
            }
            is CategoriesEvent.ShowDeleteDialog -> {
                showDeleteDialog(event)
            }
            else -> super.handleEvent(event)
        }
    }

    private fun showCreateDialog() {
        DialogBuilder(context)
            .title(R.string.title_create_category)
            .textInput(
                hint = getString(R.string.placeholder_category_name),
                allowEmpty = false,
                maxLength = NAME_MAX_LENGTH,
                callback = ::createCategory,
            )
            .showIfPossible()
    }

    private fun createCategory(name: String) {
        viewModel.onCreateDialogConfirmed(name)
    }

    private fun showContextMenu(event: CategoriesEvent.ShowContextMenu) {
        DialogBuilder(context)
            .title(event.title)
            .item(R.string.action_rename) {
                viewModel.onRenameCategoryOptionSelected(event.categoryId)
            }
            .mapIf(event.showOptionVisible) {
                item(R.string.action_show_category) {
                    viewModel.onCategoryVisibilityChanged(event.categoryId, hidden = false)
                }
            }
            .mapIf(event.hideOptionVisible) {
                item(R.string.action_hide_category) {
                    viewModel.onCategoryVisibilityChanged(event.categoryId, hidden = true)
                }
            }
            .mapIf(event.changeLayoutTypeOptionVisible) {
                item(R.string.action_change_category_layout_type) {
                    showLayoutTypeDialog(event.categoryId)
                }
                    .item(R.string.action_change_category_background) {
                        showBackgroundChangeDialog(event.categoryId)
                    }
            }
            .mapIf(event.placeOnHomeScreenOptionVisible) {
                item(R.string.action_place_category) {
                    viewModel.onPlaceOnHomeScreenSelected(event.categoryId)
                }
            }
            .mapIf(event.deleteOptionVisible) {
                item(R.string.action_delete) {
                    viewModel.onCategoryDeletionSelected(event.categoryId)
                }
            }
            .showIfPossible()
    }

    private fun showRenameDialog(event: CategoriesEvent.ShowRenameDialog) {
        DialogBuilder(context)
            .title(R.string.title_rename_category)
            .textInput(
                hint = getString(R.string.placeholder_category_name),
                prefill = event.prefill,
                allowEmpty = false,
                maxLength = NAME_MAX_LENGTH
            ) { input ->
                viewModel.onRenameDialogConfirmed(event.categoryId, newName = input)
            }
            .showIfPossible()
    }

    private fun showLayoutTypeDialog(categoryId: String) {
        DialogBuilder(context)
            .item(R.string.layout_type_linear_list) {
                viewModel.onLayoutTypeChanged(categoryId, CategoryLayoutType.LINEAR_LIST)
            }
            .item(R.string.layout_type_grid) {
                viewModel.onLayoutTypeChanged(categoryId, CategoryLayoutType.GRID)
            }
            .showIfPossible()
    }

    private fun showBackgroundChangeDialog(categoryId: String) {
        DialogBuilder(context)
            .item(R.string.category_background_type_white) {
                viewModel.onBackgroundTypeChanged(categoryId, CategoryBackgroundType.WHITE)
            }
            .item(R.string.category_background_type_black) {
                viewModel.onBackgroundTypeChanged(categoryId, CategoryBackgroundType.BLACK)
            }
            .item(R.string.category_background_type_wallpaper) {
                PermissionManager.requestFileStoragePermissionIfNeeded(this)
                viewModel.onBackgroundTypeChanged(categoryId, CategoryBackgroundType.WALLPAPER)
            }
            .showIfPossible()
    }

    private fun showDeleteDialog(event: CategoriesEvent.ShowDeleteDialog) {
        DialogBuilder(context)
            .message(R.string.confirm_delete_category_message)
            .positive(R.string.dialog_delete) {
                viewModel.onCategoryDeletionConfirmed(event.categoryId)
            }
            .negative(R.string.dialog_cancel)
            .showIfPossible()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.categories_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_help -> consume { viewModel.onHelpButtonClicked() }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(CategoriesActivity::class.java)

    companion object {

        const val EXTRA_CATEGORIES_CHANGED = "categories_changed"

        private const val NAME_MAX_LENGTH = 20
    }
}
