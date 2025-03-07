package ch.rmy.android.http_shortcuts.activities.icons

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.models.IconShape
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.icons.CropImageContract
import ch.rmy.android.http_shortcuts.utils.IconUtil

@Composable
fun IconPickerScreen() {
    val (viewModel, state) = bindViewModel<IconPickerViewState, IconPickerViewModel>()

    val pickImage = rememberLauncherForActivityResult(FilePickerUtil.PickFile) { fileUri ->
        fileUri?.let(viewModel::onImageSelected)
    }
    val context = LocalContext.current
    val cropImageIntoCustomIcon = rememberLauncherForActivityResult(
        CropImageContract(
            title = stringResource(R.string.title_edit_custom_icon),
            enforceSquare = true,
            maxSize = IconUtil.getIconSize(context),
        ),
    ) { result ->
        when (result) {
            is CropImageContract.Result.Success -> viewModel.onIconCreated(result.imageFile)
            is CropImageContract.Result.Failure -> viewModel.onIconCreationFailed()
            is CropImageContract.Result.Canceled -> Unit
        }
    }

    EventHandler { event ->
        when (event) {
            is IconPickerEvent.ShowImagePicker -> consume {
                try {
                    pickImage.launch("image/*")
                } catch (e: ActivityNotFoundException) {
                    viewModel.onImagePickerFailed()
                }
            }
            is IconPickerEvent.ShowImageCropper -> consume {
                cropImageIntoCustomIcon.launch(CropImageContract.Input(event.imageUri, circle = event.shape == IconShape.CIRCLE))
            }
            else -> false
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_custom_icons),
        actions = { viewState ->
            ToolbarIcon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.button_delete_all_unused_icons),
                enabled = viewState.isDeleteButtonEnabled,
                onClick = viewModel::onDeleteButtonClicked,
            )
        },
        floatingActionButton = {
            FloatingAddButton(
                onClick = viewModel::onAddIconButtonClicked,
                contentDescription = stringResource(R.string.accessibility_label_add_icon_fab),
            )
        },
    ) { viewState ->
        IconPickerContent(
            viewState,
            onIconClicked = viewModel::onIconClicked,
            onIconLongPressed = viewModel::onIconLongClicked,
        )
    }

    IconPickerDialogs(
        dialogState = state?.dialogState,
        onShapeSelected = viewModel::onShapeSelected,
        onDeleteConfirmed = viewModel::onDeletionConfirmed,
        onDialogDismissRequested = viewModel::onDialogDismissalRequested,
    )
}
