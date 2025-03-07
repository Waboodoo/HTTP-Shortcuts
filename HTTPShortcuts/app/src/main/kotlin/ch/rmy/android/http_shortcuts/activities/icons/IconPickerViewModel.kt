package ch.rmy.android.http_shortcuts.activities.icons

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.models.IconShape
import ch.rmy.android.http_shortcuts.activities.icons.usecases.GetIconListItemsUseCase
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.IconUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class IconPickerViewModel
@Inject
constructor(
    application: Application,
    private val getIconListItems: GetIconListItemsUseCase,
) : BaseViewModel<Unit, IconPickerViewState>(application) {

    private var selectedShape = IconShape.SQUARE

    override suspend fun initialize(data: Unit): IconPickerViewState {
        val icons = withContext(Dispatchers.IO) {
            getIconListItems()
        }
        viewModelScope.launch {
            if (icons.isEmpty()) {
                showCircleSelectionDialog()
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
        closeScreen(result = NavigationDestination.IconPicker.Result(icon))
    }

    fun onAddIconButtonClicked() = runAction {
        showCircleSelectionDialog()
    }

    private suspend fun showCircleSelectionDialog() {
        updateViewState {
            copy(dialogState = IconPickerDialogState.SelectShape)
        }
    }

    private suspend fun showImagePicker() {
        emitEvent(IconPickerEvent.ShowImagePicker)
    }

    fun onShapeSelected(iconShape: IconShape) = runAction {
        selectedShape = iconShape
        updateViewState {
            copy(dialogState = null)
        }
        showImagePicker()
    }

    fun onIconCreationFailed() = runAction {
        showSnackbar(R.string.error_set_image, long = true)
    }

    fun onImageSelected(image: Uri) = runAction {
        emitEvent(IconPickerEvent.ShowImageCropper(image, selectedShape))
    }

    fun onIconCreated(iconFile: File) = runAction {
        val iconName = IconUtil.generateCustomIconName(circular = selectedShape == IconShape.CIRCLE)
        val targetFile = File(context.filesDir, iconName)
        withContext(Dispatchers.IO) {
            iconFile.renameTo(targetFile)
        }
        val icon = ShortcutIcon.CustomIcon(iconName)

        updateViewState {
            copy(
                icons = icons.plus(IconPickerListItem(icon, isUnused = true)),
            )
        }
        selectIcon(icon)
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
            else -> Unit
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
