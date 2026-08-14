package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.sync.models.SyncState
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun SyncOverviewScreen(savedStateHandle: SavedStateHandle) {
    val (viewModel, state) = bindViewModel<SyncOverviewViewState, SyncOverviewViewModel>()

    ResultHandler(savedStateHandle) { result ->
        when (result) {
            NavigationDestination.SyncImport.RESULT_CHANGED,
            NavigationDestination.SyncExport.RESULT_CHANGED,
            -> {
                viewModel.onConfigurationChanged()
            }
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.settings_automatic_import_export),
        actions = {
            ToolbarIcon(
                painterResource(R.drawable.outline_help_24),
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
        floatingActionButton = {
            var previousSyncType by remember {
                mutableStateOf(state?.syncType)
            }
            SideEffect(state?.syncType) {
                if (state?.syncType != null) {
                    previousSyncType = state.syncType
                }
            }
            AnimatedVisibility(
                modifier = Modifier.offset(16.dp, 16.dp),
                visible = state?.syncType != null && state.isConfigValid,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                val syncType = state?.syncType ?: previousSyncType ?: return@AnimatedVisibility
                val syncState = state?.syncState ?: SyncState.IDLE
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = viewModel::onSyncNowClicked,
                    icon = {
                        Icon(
                            modifier = if (syncState == SyncState.SYNCING) {
                                Modifier.rotating()
                            } else {
                                Modifier
                            },
                            painter = painterResource(R.drawable.outline_sync_24),
                            contentDescription = null,
                        )
                    },
                    text = {
                        when (syncState) {
                            SyncState.IDLE -> {
                                when (syncType) {
                                    SyncType.IMPORT -> Text(
                                        text = stringResource(R.string.fab_automatic_import_now),
                                    )
                                    SyncType.EXPORT -> Text(
                                        text = stringResource(R.string.fab_automatic_export_now),
                                    )
                                }
                            }
                            SyncState.SYNCING -> {
                                when (syncType) {
                                    SyncType.IMPORT -> Text(
                                        text = stringResource(R.string.import_in_progress),
                                    )
                                    SyncType.EXPORT -> Text(
                                        text = stringResource(R.string.export_in_progress),
                                    )
                                }
                            }
                        }
                    },
                )
            }
        },
    ) { viewState ->
        SyncOverviewContent(
            viewState,
            onSyncTypeSelected = viewModel::onSyncTypeSelected,
            onConfigureImportClicked = viewModel::onConfigureImportClicked,
            onConfigureExportClicked = viewModel::onConfigureExportClicked,
            onFailureInfoClicked = viewModel::onFailureInfoClicked,
        )
    }
}

@Composable
private fun Modifier.rotating(): Modifier {
    var currentRotation by remember { mutableFloatStateOf(0f) }
    val rotation = remember { Animatable(currentRotation) }
    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = currentRotation + 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        ) {
            currentRotation = value
        }
    }
    return rotate(360f - currentRotation)
}
