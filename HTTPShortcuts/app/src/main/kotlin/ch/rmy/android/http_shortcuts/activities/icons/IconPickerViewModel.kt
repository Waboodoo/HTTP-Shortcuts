package ch.rmy.android.http_shortcuts.activities.icons

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.usecases.GetBulkDeletionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.icons.usecases.GetDeletionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.icons.usecases.GetIconListItemsUseCase
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.usecases.GetUsedCustomIconsUseCase
import ch.rmy.android.http_shortcuts.utils.IconUtil
import com.yalantis.ucrop.UCrop
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.io.File

class IconPickerViewModel(application: Application) : BaseViewModel<Unit, IconPickerViewState>(application), WithDialog {

    private val appRepository = AppRepository()
    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val getUsedCustomIcons = GetUsedCustomIconsUseCase(appRepository, temporaryShortcutRepository)
    private val getIconListItems = GetIconListItemsUseCase(context, getUsedCustomIcons)
    private val getDeletionDialog = GetDeletionDialogUseCase()
    private val getBulkDeletionDialog = GetBulkDeletionDialogUseCase()

    private lateinit var icons: List<IconPickerListItem>

    override fun onInitializationStarted(data: Unit) {
        getIconListItems()
            .subscribe(
                { icons ->
                    this.icons = icons
                    finalizeInitialization()
                },
                { error ->
                    finish()
                    handleUnexpectedError(error)
                },
            )
            .attachTo(destroyer)
    }

    override fun initViewState() = IconPickerViewState(
        icons = icons,
    )

    override fun onInitialized() {
        if (currentViewState.icons.isEmpty()) {
            showImagePicker()
        }
    }

    fun onIconClicked(icon: ShortcutIcon.CustomIcon) {
        selectIcon(icon)
    }

    private fun selectIcon(icon: ShortcutIcon.CustomIcon) {
        finish(
            result = Activity.RESULT_OK,
            intent = IconPickerActivity.Result.create(icon),
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
        val iconSize = IconUtil.getIconSize(context)
        emitEvent(
            ViewModelEvent.OpenActivity(
                object : IntentBuilder {
                    override fun build(context: Context): Intent =
                        UCrop.of(image, createNewIconFile())
                            .withOptions(
                                UCrop.Options().apply {
                                    setToolbarTitle(context.getString(R.string.title_edit_custom_icon))
                                    setCompressionQuality(100)
                                    setCompressionFormat(Bitmap.CompressFormat.PNG)
                                }
                            )
                            .withAspectRatio(1f, 1f)
                            .withMaxResultSize(iconSize, iconSize)
                            .getIntent(context)
                },
                requestCode = IconPickerActivity.REQUEST_CROP_IMAGE,
            )
        )
    }

    private fun createNewIconFile(): Uri =
        Uri.fromFile(File(context.filesDir, IconUtil.generateCustomIconName()))

    fun onIconCreated(icon: ShortcutIcon.CustomIcon) {
        val isFirstIcon = currentViewState.icons.isEmpty()
        updateViewState {
            copy(
                icons = icons.plus(IconPickerListItem(icon, isUnused = true)),
            )
        }
        if (isFirstIcon) {
            selectIcon(icon)
        }
    }

    fun onImagePickerFailed() {
        showSnackbar(R.string.error_not_supported)
    }

    fun onIconLongClicked(icon: ShortcutIcon.CustomIcon) {
        val isUnused = currentViewState.icons.find { it.icon == icon }?.isUnused ?: return
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

    private fun onDeletionConfirmed(icon: ShortcutIcon.CustomIcon) {
        icon.getFile(context)?.delete()
        updateViewState {
            copy(
                icons = icons.filter { it.icon != icon },
            )
        }
    }

    override fun onDialogDismissed(id: String?) {
        updateViewState {
            copy(dialogState = null)
        }
    }

    fun onDeleteButtonClicked() {
        updateViewState {
            copy(dialogState = getBulkDeletionDialog(::onBulkDeletionConfirmed))
        }
    }

    private fun onBulkDeletionConfirmed() {
        val icons = currentViewState.icons.filter { it.isUnused }
        Completable.fromAction {
            icons.forEach {
                it.icon.getFile(context)?.delete()
            }
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
            .attachTo(destroyer)
        updateViewState {
            copy(
                icons = this.icons.filterNot { it.isUnused },
            )
        }
    }
}
