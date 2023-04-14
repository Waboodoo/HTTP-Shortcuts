package ch.rmy.android.http_shortcuts.activities.icons

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.icons.CropIconContract

@Composable
fun ScreenScope.IconPickerScreen() {
    val (viewModel, state) = bindViewModel<IconPickerViewState, IconPickerViewModel>()

    val pickImage = rememberLauncherForActivityResult(FilePickerUtil.PickFile) { fileUri ->
        fileUri?.let(viewModel::onImageSelected)
    }
    val cropImageIntoCustomIcon = rememberLauncherForActivityResult(CropIconContract) { result ->
        when (result) {
            is CropIconContract.Result.Success -> viewModel.onIconCreated(result.iconUri)
            is CropIconContract.Result.Failure -> viewModel.onIconCreationFailed()
            is CropIconContract.Result.Canceled -> Unit
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
                cropImageIntoCustomIcon.launch(event.imageUri)
            }
            else -> false
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_custom_icons),
        actions = { viewState ->
            if (viewState.isDeleteButtonVisible) {
                ToolbarIcon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.button_delete_all_unused_icons),
                    onClick = viewModel::onDeleteButtonClicked,
                )
            }
        },
        floatingActionButton = {
            FloatingAddButton(onClick = viewModel::onAddIconButtonClicked)
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
        onDeleteConfirmed = viewModel::onDeletionConfirmed,
        onDialogDismissRequested = viewModel::onDialogDismissalRequested,
    )
}
