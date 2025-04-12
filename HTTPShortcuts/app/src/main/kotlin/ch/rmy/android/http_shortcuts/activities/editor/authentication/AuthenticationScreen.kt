package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun AuthenticationScreen(
    savedStateHandle: SavedStateHandle,
) {
    val (viewModel, state) = bindViewModel<AuthenticationViewState, AuthenticationViewModel>()

    val openFilePickerForCertificate = rememberLauncherForActivityResult(FilePickerUtil.PickFile) { fileUri ->
        fileUri?.let(viewModel::onCertificateFileSelected)
    }

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.section_authentication),
    ) { viewState ->
        AuthenticationContent(
            savedStateHandle = savedStateHandle,
            shortcutExecutionType = viewState.shortcutExecutionType,
            authenticationType = viewState.authenticationType,
            username = viewState.username,
            password = viewState.password,
            token = viewState.token,
            clientCertParams = viewState.clientCertParams,
            isClientCertButtonEnabled = viewState.isClientCertButtonEnabled,
            onAuthenticationTypeChanged = viewModel::onAuthenticationTypeChanged,
            onUsernameChanged = viewModel::onUsernameChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onTokenChanged = viewModel::onTokenChanged,
            onClientCertButtonClicked = viewModel::onClientCertButtonClicked,
        )
    }

    AuthenticationDialogs(
        dialogState = state?.dialogState,
        onFromSystemOptionSelected = viewModel::onPickCertificateFromSystemOptionSelected,
        onFromFileOptionSelected = {
            try {
                viewModel.onDialogDismissed()
                openFilePickerForCertificate.launch("application/x-pkcs12")
            } catch (e: ActivityNotFoundException) {
                viewModel.onCertificateFilePickerFailed()
            }
        },
        onPasswordConfirmed = viewModel::onCertPasswordConfirmed,
        onDismissed = viewModel::onDialogDismissed,
    )
}
