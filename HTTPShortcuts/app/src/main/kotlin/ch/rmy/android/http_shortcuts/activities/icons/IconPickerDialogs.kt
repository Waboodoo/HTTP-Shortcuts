package ch.rmy.android.http_shortcuts.activities.icons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Square
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.models.IconShape
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun IconPickerDialogs(
    dialogState: IconPickerDialogState?,
    onShapeSelected: (IconShape) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDialogDismissRequested: () -> Unit,
) {
    when (dialogState) {
        is IconPickerDialogState.SelectShape -> {
            SelectShapeDialog(
                onShapeSelected = onShapeSelected,
                onDismissRequested = onDialogDismissRequested,
            )
        }
        is IconPickerDialogState.DeleteIcon -> {
            DeleteIconDialog(
                stillInUseWarning = dialogState.stillInUseWarning,
                onConfirm = onDeleteConfirmed,
                onDismissRequested = onDialogDismissRequested,
            )
        }
        is IconPickerDialogState.BulkDelete -> {
            BulkDeleteDialog(
                onConfirm = onDeleteConfirmed,
                onDismissRequested = onDialogDismissRequested,
            )
        }
        null -> Unit
    }
}

@Composable
private fun SelectShapeDialog(
    onShapeSelected: (IconShape) -> Unit,
    onDismissRequested: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.dialog_title_select_icon_shape),
        onDismissRequest = onDismissRequested,
    ) {
        Row(
            modifier = Modifier
                .width(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Spacing.BIG),
        ) {
            ShapeButton(
                text = stringResource(R.string.icon_shape_square),
                icon = Icons.Outlined.Square,
                onClick = {
                    onShapeSelected(IconShape.SQUARE)
                },
            )
            ShapeButton(
                text = stringResource(R.string.icon_shape_round),
                icon = Icons.Outlined.Circle,
                onClick = {
                    onShapeSelected(IconShape.CIRCLE)
                },
            )
        }
    }
}

@Composable
private fun ShapeButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
            )
            .padding(horizontal = Spacing.MEDIUM),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        Icon(
            modifier = Modifier.size(60.dp),
            imageVector = icon,
            contentDescription = null,
        )
        Text(
            text,
            fontSize = FontSize.BIG,
        )
    }
}

@Composable
private fun DeleteIconDialog(
    stillInUseWarning: Boolean,
    onConfirm: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    ConfirmDialog(
        message = stringResource(
            if (stillInUseWarning) {
                R.string.confirm_delete_custom_icon_still_in_use_message
            } else {
                R.string.confirm_delete_custom_icon_message
            },
        ),
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirm,
        onDismissRequest = onDismissRequested,
    )
}

@Composable
private fun BulkDeleteDialog(
    onConfirm: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    ConfirmDialog(
        message = stringResource(R.string.confirm_delete_all_unused_custom_icons_message),
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirm,
        onDismissRequest = onDismissRequested,
    )
}

@Preview
@Composable
private fun SelectShapeDialog_Preview() {
    SelectShapeDialog(
        onShapeSelected = {},
        onDismissRequested = {},
    )
}
