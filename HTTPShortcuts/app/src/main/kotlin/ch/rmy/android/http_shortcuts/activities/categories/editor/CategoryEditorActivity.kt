package ch.rmy.android.http_shortcuts.activities.categories.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.isVisible
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setMaxLength
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.categories.editor.models.CategoryBackground
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
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

        binding.inputCategoryName.setMaxLength(CategoryModel.NAME_MAX_LENGTH)

        binding.inputLayoutType.setItemsFromPairs(
            CategoryLayoutType.LINEAR_LIST.type to getString(R.string.layout_type_linear_list),
            CategoryLayoutType.DENSE_GRID.type to getString(R.string.layout_type_dense_grid),
            CategoryLayoutType.MEDIUM_GRID.type to getString(R.string.layout_type_medium_grid),
            CategoryLayoutType.WIDE_GRID.type to getString(R.string.layout_type_wide_grid),
        )
        binding.inputBackgroundType.setItemsFromPairs(
            CategoryBackground.DEFAULT.name to getString(R.string.category_background_type_default),
            CategoryBackground.COLOR.name to getString(R.string.category_background_type_color),
            CategoryBackground.WALLPAPER.name to getString(R.string.category_background_type_wallpaper),
        )
        binding.inputClickBehavior.setItemsFromPairs(
            SHORTCUT_BEHAVIOR_DEFAULT to getString(R.string.settings_click_behavior_global_default),
            ShortcutClickBehavior.RUN.type to getString(R.string.settings_click_behavior_run),
            ShortcutClickBehavior.EDIT.type to getString(R.string.settings_click_behavior_edit),
            ShortcutClickBehavior.MENU.type to getString(R.string.settings_click_behavior_menu),
        )
        binding.inputColor.setOnClickListener {
            viewModel.onColorButtonClicked()
        }
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
                viewModel.onBackgroundChanged(CategoryBackground.valueOf(it))
            }
            .attachTo(destroyer)
        binding.inputClickBehavior.selectionChanges
            .subscribe { clickBehavior ->
                viewModel.onClickBehaviorChanged(
                    clickBehavior.takeUnless { it == SHORTCUT_BEHAVIOR_DEFAULT }
                        ?.let(ShortcutClickBehavior::parse)
                )
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.loadingIndicator.isVisible = false
            setTitle(viewState.toolbarTitle)
            binding.inputCategoryName.setTextSafely(viewState.categoryName)
            binding.inputLayoutType.selectedItem = viewState.categoryLayoutType.type
            binding.inputBackgroundType.selectedItem = viewState.categoryBackground.name
            binding.inputClickBehavior.selectedItem = viewState.categoryClickBehavior?.type ?: SHORTCUT_BEHAVIOR_DEFAULT
            binding.inputColor.isVisible = viewState.colorButtonVisible
            if (viewState.colorButtonVisible) {
                binding.inputColor.text = viewState.backgroundColorAsText
                binding.inputColor.setBackgroundColor(viewState.backgroundColor)
            }
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

    class IntentBuilder : BaseIntentBuilder(CategoryEditorActivity::class) {
        fun categoryId(categoryId: CategoryId) = also {
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
        }
    }

    companion object {
        private const val EXTRA_CATEGORY_ID = "category_id"
        private const val SHORTCUT_BEHAVIOR_DEFAULT = "default"
    }
}
