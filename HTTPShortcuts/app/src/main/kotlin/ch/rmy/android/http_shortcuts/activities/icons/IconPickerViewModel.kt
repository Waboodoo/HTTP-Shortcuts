package ch.rmy.android.http_shortcuts.activities.icons

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.usecases.GetBulkDeletionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.icons.usecases.GetDeletionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.icons.usecases.GetIconListItemsUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class IconPickerViewModel(application: Application) : BaseViewModel<Unit, IconPickerViewState>(application), WithDialog {

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var getIconListItems: GetIconListItemsUseCase

    @Inject
    lateinit var getDeletionDialog: GetDeletionDialogUseCase

    @Inject
    lateinit var getBulkDeletionDialog: GetBulkDeletionDialogUseCase

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var icons: List<IconPickerListItem>

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

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
            val message = StringResLocalizable(
                if (isUnused) {
                    R.string.confirm_delete_custom_icon_message
                } else {
                    R.string.confirm_delete_custom_icon_still_in_use_message
                }
            )
            updateViewState {
                copy(dialogState = getDeletionDialog(icon, message, ::onDeletionConfirmed))
            }
        }
    }

    private fun onDeletionConfirmed(icon: ShortcutIcon.CustomIcon) {
        icon.getFile(context)?.delete()
        updateViewState {
            copy(
                icons = icons.filter { it.icon != icon },
            )
        }
    }

    fun onDeleteButtonClicked() {
        updateViewState {
            copy(dialogState = getBulkDeletionDialog(::onBulkDeletionConfirmed))
        }
    }

    private fun onBulkDeletionConfirmed() {
        doWithViewState { viewState ->
            val icons = viewState.icons.filter { it.isUnused }
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    icons.forEach {
                        it.icon.getFile(context)?.delete()
                    }
                }
            }
            updateViewState {
                copy(
                    icons = this.icons.filterNot { it.isUnused },
                )
            }
        }
    }
}
