package ch.rmy.android.http_shortcuts.activities.categories.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.isVisible
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.databinding.ActivityCategoryEditorBinding
import ch.rmy.android.http_shortcuts.utils.PermissionManager

class CategoryEditorActivity : BaseActivity() {

    override val navigateUpIcon = R.drawable.ic_clear

    private val viewModel: CategoryEditorViewModel by bindViewModel()

    private lateinit var binding: ActivityCategoryEditorBinding
    private var saveButton: MenuItem? = null

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize(
            CategoryEditorViewModel.InitData(
                categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID),
            )
        )
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityCategoryEditorBinding.inflate(layoutInflater))
        title = ""

        binding.inputLayoutType.setItemsFromPairs(
            listOf(
                CategoryLayoutType.LINEAR_LIST.type to getString(R.string.layout_type_linear_list),
                CategoryLayoutType.GRID.type to getString(R.string.layout_type_grid),
            )
        )
        binding.inputBackgroundType.setItemsFromPairs(
            listOf(
                CategoryBackgroundType.WHITE.type to getString(R.string.category_background_type_default),
                CategoryBackgroundType.BLACK.type to getString(R.string.category_background_type_black),
                CategoryBackgroundType.WALLPAPER.type to getString(R.string.category_background_type_wallpaper),
            )
        )
    }

    private fun initUserInputBindings() {
        binding.inputCategoryName.observeTextChanges()
            .subscribe {
                viewModel.onCategoryNameChanged(it.toString())
            }
            .attachTo(destroyer)

        binding.inputLayoutType.selectionChanges
            .subscribe {
                viewModel.onLayoutTypeChanged(CategoryLayoutType.parse(it))
            }
            .attachTo(destroyer)

        binding.inputBackgroundType.selectionChanges
            .subscribe {
                viewModel.onBackgroundChanged(CategoryBackgroundType.parse(it))
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.loadingIndicator.isVisible = false
            setTitle(viewState.toolbarTitle)
            binding.inputCategoryName.setTextSafely(viewState.categoryName)
            binding.inputLayoutType.selectedItem = viewState.categoryLayoutType.type
            binding.inputBackgroundType.selectedItem = viewState.categoryBackgroundType.type
            saveButton?.isVisible = viewState.saveButtonVisible
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is CategoryEditorEvent.RequestFilePermissionsIfNeeded -> {
                PermissionManager.requestFileStoragePermissionIfNeeded(this)
            }
            else -> super.handleEvent(event)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.category_editor_activity_menu, menu)
        saveButton = menu.findItem(R.id.action_save_category)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_save_category -> consume(viewModel::onSaveButtonClicked)
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    object OpenCategoryEditor : BaseActivityResultContract<IntentBuilder, Boolean>(::IntentBuilder) {
        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            resultCode == Activity.RESULT_OK
    }

    class IntentBuilder : BaseIntentBuilder(CategoryEditorActivity::class.java) {
        fun categoryId(categoryId: CategoryId) = also {
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
        }
    }

    companion object {
        private const val EXTRA_CATEGORY_ID = "category_id"
    }
}
