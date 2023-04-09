package ch.rmy.android.http_shortcuts.activities.icons

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.usecases.GetIconListItemsUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import kotlinx.coroutines.CancellationException
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

    private lateinit var icons: List<IconPickerListItem>

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch {
            try {
                icons = getIconListItems()
                finalizeInitialization()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                finish()
                handleUnexpectedError(e)
            }
        }
    }

    override fun initViewState() = IconPickerViewState(
        icons = icons,
    )

    override fun onInitialized() {
        doWithViewState { viewState ->
            if (viewState.icons.isEmpty()) {
                showImagePicker()
            }
        }
    }

    fun onIconClicked(icon: ShortcutIcon.CustomIcon) {
        selectIcon(icon)
    }

    private fun selectIcon(icon: ShortcutIcon.CustomIcon) {
        finishWithOkResult(
            IconPickerActivity.PickIcon.createResult(icon),
        )
    }

    fun onAddIconButtonClicked() {
        showImagePicker()
    }

    private fun showImagePicker() {
        emitEvent(IconPickerEvent.ShowImagePicker)
    }

    fun onIconCreationFailed() {
        showSnackbar(R.string.error_set_image, long = true)
    }

    fun onImageSelected(image: Uri) {
        emitEvent(IconPickerEvent.ShowImageCropper(image))
    }

    fun onIconCreated(iconUri: Uri) {
        doWithViewState { viewState ->
            val iconName = IconUtil.generateCustomIconName()
            iconUri.toFile().renameTo(File(context.filesDir, iconName))
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
    }

    fun onImagePickerFailed() {
        showSnackbar(R.string.error_not_supported)
    }

    fun onIconLongClicked(icon: ShortcutIcon.CustomIcon) {
        doWithViewState { viewState ->
            val isUnused = viewState.icons.find { it.icon == icon }?.isUnused ?: return@doWithViewState
            updateViewState {
                copy(dialogState = IconPickerDialogState.DeleteIcon(icon, !isUnused))
            }
        }
    }

    fun onDeleteButtonClicked() {
        updateViewState {
            copy(dialogState = IconPickerDialogState.BulkDelete)
        }
    }

    fun onDeletionConfirmed() {
        doWithViewState { viewState ->
            when (viewState.dialogState) {
                is IconPickerDialogState.BulkDelete -> onBulkDeletionConfirmed()
                is IconPickerDialogState.DeleteIcon -> onDeletionConfirmed(viewState.dialogState.icon)
                null -> Unit
            }
        }
    }

    private fun onDeletionConfirmed(icon: ShortcutIcon.CustomIcon) {
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

    private fun onBulkDeletionConfirmed() {
        doWithViewState { viewState ->
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
    }

    fun onDialogDismissalRequested() {
        updateViewState {
            copy(
                dialogState = null,
            )
        }
    }
}
