package ch.rmy.android.http_shortcuts.activities.icons

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.isVisible
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityIconPickerBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.icons.CropIconContract
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.GridLayoutManager

class IconPickerActivity : BaseActivity() {

    private val pickImage = registerForActivityResult(FilePickerUtil.PickFile) { fileUri ->
        fileUri?.let(viewModel::onImageSelected)
    }
    private val cropImageIntoCustomIcon = registerForActivityResult(CropIconContract) { result ->
        when (result) {
            is CropIconContract.Result.Success -> viewModel.onIconCreated(result.iconUri)
            is CropIconContract.Result.Failure -> viewModel.onIconCreationFailed()
            is CropIconContract.Result.Canceled -> Unit
        }
    }

    private val viewModel: IconPickerViewModel by bindViewModel()

    private lateinit var binding: ActivityIconPickerBinding
    private lateinit var adapter: IconPickerAdapter
    private lateinit var layoutManager: GridLayoutManager
    private var deleteMenuItem: MenuItem? = null

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityIconPickerBinding.inflate(layoutInflater))
        adapter = IconPickerAdapter()
        layoutManager = GridLayoutManager(context, R.dimen.grid_layout_custom_icon_width)
        binding.iconList.layoutManager = layoutManager
        binding.iconList.setHasFixedSize(true)
        binding.iconList.adapter = adapter

        binding.buttonCreateIcon.applyTheme(themeHelper)
    }

    private fun initUserInputBindings() {
        adapter.userEvents.observe(this) { event ->
            when (event) {
                is IconPickerAdapter.UserEvent.IconClicked -> viewModel.onIconClicked(event.icon)
                is IconPickerAdapter.UserEvent.IconLongClicked -> viewModel.onIconLongClicked(event.icon)
            }
        }

        binding.buttonCreateIcon.setOnClickListener {
            viewModel.onAddIconButtonClicked()
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.loadingIndicator.isVisible = false
            binding.buttonCreateIcon.isVisible = true
            adapter.items = viewState.icons
            layoutManager.setEmpty(viewState.isEmptyStateVisible)
            deleteMenuItem?.isVisible = viewState.isDeleteButtonVisible
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is IconPickerEvent.ShowImagePicker -> openImagePicker()
            is IconPickerEvent.ShowImageCropper -> cropImageIntoCustomIcon.launch(event.imageUri)
            else -> super.handleEvent(event)
        }
    }

    private fun openImagePicker() {
        try {
            pickImage.launch("image/*")
        } catch (e: ActivityNotFoundException) {
            viewModel.onImagePickerFailed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.icon_picker_menu, menu)
        deleteMenuItem = menu.findItem(R.id.action_delete_unused_icons)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_delete_unused_icons -> consume {
                logInfo("Clicked Delete button in icon picker")
                viewModel.onDeleteButtonClicked()
            }
            else -> super.onOptionsItemSelected(item)
        }

    object PickIcon : BaseActivityResultContract<IntentBuilder, ShortcutIcon?>(::IntentBuilder) {
        override fun parseResult(resultCode: Int, intent: Intent?): ShortcutIcon? =
            intent?.getStringExtra(EXTRA_ICON)?.let(ShortcutIcon::fromName)

        fun createResult(icon: ShortcutIcon) =
            createIntent {
                putExtra(EXTRA_ICON, icon.toString())
            }

        private const val EXTRA_ICON = "icon"
    }

    class IntentBuilder : BaseIntentBuilder(IconPickerActivity::class)
}
