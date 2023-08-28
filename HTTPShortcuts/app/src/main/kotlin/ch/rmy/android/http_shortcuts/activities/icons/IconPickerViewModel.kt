package ch.rmy.android.http_shortcuts.activities.icons

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.usecases.GetIconListItemsUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class IconPickerViewModel(application: Application) : BaseViewModel<Unit, IconPickerViewState>(application) {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var getIconListItems: GetIconListItemsUseCase

    init {
        getApplicationComponent().inject(this)
    }

    override suspend fun initialize(data: Unit): IconPickerViewState {
        val icons = getIconListItems()
        viewModelScope.launch {
            if (icons.isEmpty()) {
                showImagePicker()
            }
        }
        return IconPickerViewState(
            icons = icons,
        )
    }

    fun onIconClicked(icon: ShortcutIcon.CustomIcon) = runAction {
        selectIcon(icon)
    }

    private suspend fun selectIcon(icon: ShortcutIcon.CustomIcon) {
        finishWithOkResult(
            IconPickerActivity.PickIcon.createResult(icon),
        )
    }

    fun onAddIconButtonClicked() = runAction {
        showImagePicker()
    }

    private suspend fun showImagePicker() {
        emitEvent(IconPickerEvent.ShowImagePicker)
    }

    fun onIconCreationFailed() = runAction {
        showSnackbar(R.string.error_set_image, long = true)
    }

    fun onImageSelected(image: Uri) = runAction {
        emitEvent(IconPickerEvent.ShowImageCropper(image))
    }

    fun onIconCreated(iconFile: File) = runAction {
        val iconName = IconUtil.generateCustomIconName()
        iconFile.renameTo(File(context.filesDir, iconName))
        val icon = ShortcutIcon.CustomIcon(iconName)

        val isFirstIcon = viewState.icons.isEmpty()
        updateViewState {
            copy(
                icons = icons.plus(IconPickerListItem(icon, isUnused = true)),
            )
        }
        if (isFirstIcon) {
            selectIcon(icon)
        }
    }

    fun onImagePickerFailed() = runAction {
        showSnackbar(R.string.error_not_supported)
    }

    fun onIconLongClicked(icon: ShortcutIcon.CustomIcon) = runAction {
        val isUnused = viewState.icons.find { it.icon == icon }?.isUnused ?: skipAction()
        updateViewState {
            copy(dialogState = IconPickerDialogState.DeleteIcon(icon, !isUnused))
        }
    }

    fun onDeleteButtonClicked() = runAction {
        updateViewState {
            copy(dialogState = IconPickerDialogState.BulkDelete)
        }
    }

    fun onDeletionConfirmed() = runAction {
        when (val dialogState = viewState.dialogState) {
            is IconPickerDialogState.BulkDelete -> onBulkDeletionConfirmed()
            is IconPickerDialogState.DeleteIcon -> onDeletionConfirmed(dialogState.icon)
            null -> Unit
        }
    }

    private suspend fun onDeletionConfirmed(icon: ShortcutIcon.CustomIcon) {
        viewModelScope.launch(Dispatchers.IO) {
            icon.getFile(context)?.delete()
        }
        updateViewState {
            copy(
                icons = icons.filter { it.icon != icon },
                dialogState = null,
            )
        }
    }

    private suspend fun ViewModelScope<IconPickerViewState>.onBulkDeletionConfirmed() {
        val icons = viewState.icons.filter { it.isUnused }
        viewModelScope.launch(Dispatchers.IO) {
            icons.forEach {
                it.icon.getFile(context)?.delete()
            }
        }
        updateViewState {
            copy(
                icons = this.icons.filterNot { it.isUnused },
                dialogState = null,
            )
        }
    }

    fun onDialogDismissalRequested() = runAction {
        updateViewState {
            copy(
                dialogState = null,
            )
        }
    }
}
