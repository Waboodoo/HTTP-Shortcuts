package ch.rmy.android.http_shortcuts.activities.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ChangeTitleDialog
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.TextInputDialog
import ch.rmy.android.http_shortcuts.components.UnlockAppDialog
import ch.rmy.android.http_shortcuts.data.enums.AppIconType
import ch.rmy.android.http_shortcuts.extensions.runIf
import ch.rmy.android.http_shortcuts.utils.Validation

@Composable
fun SettingsDialogs(
    dialogState: SettingsDialogState?,
    onLockConfirmed: (String, Boolean) -> Unit,
    onLockRemoved: () -> Unit,
    onUnlockDialogSubmitted: (String) -> Unit,
    onTitleChangeConfirmed: (String) -> Unit,
    onUserAgentChangeConfirmed: (String) -> Unit,
    onClearCookiesConfirmed: () -> Unit,
    onAppIconTypeSelected: (AppIconType) -> Unit,
    onDismissalRequested: () -> Unit,
) {
    when (dialogState) {
        is SettingsDialogState.ChangeTitle -> {
            ChangeTitleDialog(
                initialValue = dialogState.oldTitle,
                onConfirm = onTitleChangeConfirmed,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.ChangeUserAgent -> {
            ChangeUserAgentDialog(
                initialValue = dialogState.oldUserAgent,
                placeholder = dialogState.placeholder,
                onConfirm = onUserAgentChangeConfirmed,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.LockApp -> {
            LockAppDialog(
                canUseBiometrics = dialogState.canUseBiometrics,
                hasLock = dialogState.hasLock,
                usesBiometrics = dialogState.usesBiometrics,
                onConfirm = onLockConfirmed,
                onRemove = onLockRemoved,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.Unlock -> {
            UnlockAppDialog(
                tryAgain = dialogState.tryAgain,
                onSubmitted = onUnlockDialogSubmitted,
                onDismissed = onDismissalRequested,
            )
        }
        is SettingsDialogState.ClearCookies -> {
            ClearCookiesDialog(
                onConfirm = onClearCookiesConfirmed,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.SelectAppIcon -> {
            SelectAppIconDialog(
                current = dialogState.current,
                onSelect = onAppIconTypeSelected,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.AppIconInfo -> {
            ConfirmDialog(
                message = stringResource(R.string.instructions_apply_new_app_icon),
                onConfirmRequest = {
                    onAppIconTypeSelected(dialogState.selected)
                },
                onDismissRequest = onDismissalRequested,
            )
        }
        is SettingsDialogState.Progress -> {
            ProgressDialog(
                onDismissRequest = {},
            )
        }
        null -> Unit
    }
}

@Composable
private fun ChangeUserAgentDialog(
    initialValue: String,
    placeholder: String,
    onConfirm: (String) -> Unit,
    onDismissalRequested: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.title_set_user_agent),
        message = stringResource(R.string.instructions_set_user_agent),
        initialValue = initialValue,
        placeholder = placeholder,
        transformValue = {
            it.filter(Validation::isValidInHeaderValue).take(300)
        },
        imeAction = ImeAction.Go,
        onDismissRequest = { text ->
            if (text != null) {
                onConfirm(text)
            } else {
                onDismissalRequested()
            }
        },
    )
}

@Composable
private fun LockAppDialog(
    canUseBiometrics: Boolean,
    hasLock: Boolean,
    usesBiometrics: Boolean,
    onConfirm: (password: String, useBiometrics: Boolean) -> Unit,
    onRemove: () -> Unit,
    onDismissalRequested: () -> Unit,
) {
    var useBiometrics by remember {
        mutableStateOf(canUseBiometrics && usesBiometrics)
    }

    TextInputDialog(
        title = stringResource(R.string.dialog_title_lock_app),
        message = stringResource(R.string.dialog_text_lock_app),
        confirmButton = stringResource(R.string.button_lock_app),
        allowEmpty = false,
        monospace = true,
        singleLine = true,
        imeAction = ImeAction.Go,
        keyboardType = KeyboardType.Password,
        transformValue = {
            it.take(50)
        },
        bottomContent = {
            if (canUseBiometrics) {
                Checkbox(
                    label = stringResource(R.string.label_app_lock_use_biometrics),
                    checked = useBiometrics,
                    onCheckedChange = {
                        useBiometrics = it
                    },
                )
            }
        },
        dismissButton = if (hasLock) {
            {
                TextButton(
                    onClick = onRemove,
                ) {
                    Text(stringResource(R.string.dialog_remove))
                }
            }
        } else {
            null
        },
        onDismissRequest = { text ->
            if (text != null) {
                onConfirm(text, useBiometrics)
            } else {
                onDismissalRequested()
            }
        },
    )
}

@Composable
private fun ClearCookiesDialog(
    onConfirm: () -> Unit,
    onDismissalRequested: () -> Unit,
) {
    ConfirmDialog(
        message = stringResource(R.string.confirm_clear_cookies_message),
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirm,
        onDismissRequest = onDismissalRequested,
    )
}

@Composable
private fun SelectAppIconDialog(
    current: AppIconType,
    onSelect: (AppIconType) -> Unit,
    onDismissalRequested: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.dialog_title_select_app_icon),
        scrolling = false,
        onDismissRequest = onDismissalRequested,
    ) { horizontalPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 64.dp),
            contentPadding = PaddingValues(Spacing.MEDIUM) + PaddingValues(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
            horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
        ) {
            items(
                items = AppIconType.entries,
                key = AppIconType::type,
            ) { appIconType ->
                AppIconOption(
                    selected = appIconType == current,
                    onClick = {
                        onSelect(appIconType)
                    },
                    foreground = painterResource(appIconType.foregroundResource),
                    background = colorResource(appIconType.backgroundColorResource),
                )
            }
        }
    }
}

@Stable
private val AppIconType.foregroundResource: Int
    get() = when (this) {
        AppIconType.DEFAULT -> R.drawable.ic_launcher_foreground
        AppIconType.BLACK_AND_WHITE -> R.drawable.ic_launcher_foreground_white
        AppIconType.WHITE_AND_BLACK -> R.drawable.ic_launcher_foreground_black
        AppIconType.SOLARIZED -> R.drawable.ic_launcher_foreground_solarized
        AppIconType.HACKER -> R.drawable.ic_launcher_foreground_hacker
        AppIconType.GREEN -> R.drawable.ic_launcher_foreground_green
    }

@Stable
private val AppIconType.backgroundColorResource: Int
    get() = when (this) {
        AppIconType.DEFAULT -> R.color.brand_icon
        AppIconType.BLACK_AND_WHITE -> R.color.black
        AppIconType.WHITE_AND_BLACK -> R.color.white
        AppIconType.SOLARIZED -> R.color.beige
        AppIconType.HACKER -> R.color.black
        AppIconType.GREEN -> R.color.green
    }

@Composable
private fun AppIconOption(
    selected: Boolean,
    foreground: Painter,
    background: Color,
    onClick: () -> Unit,
) {
    Image(
        modifier = Modifier
            .padding(Spacing.TINY)
            .size(56.dp)
            .aspectRatio(1f)
            .runIf(selected) {
                shadow(elevation = 4.dp, shape = RoundedCornerShape(percent = 30))
            }
            .background(background, shape = RoundedCornerShape(percent = 30))
            .clickable(onClick = onClick),
        painter = foreground,
        contentDescription = null,
    )
}
