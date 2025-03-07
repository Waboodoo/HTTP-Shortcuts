package ch.rmy.android.http_shortcuts.components

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.utils.SnackbarManager
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.runIf
import kotlinx.coroutines.launch

enum class BackButton {
    ARROW,
    CROSS,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun <T : Any> SimpleScaffold(
    viewState: T?,
    title: String,
    subtitle: String? = null,
    backButton: BackButton? = BackButton.ARROW,
    floatingActionButton: @Composable () -> Unit = {},
    onTitleClicked: (() -> Unit)? = null,
    actions: @Composable RowScope.(viewState: T) -> Unit = {},
    content: @Composable (viewState: T) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = LocalActivity.current

    val showSnackbar = remember(snackbarHostState, scope) {
        { message: String, long: Boolean ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = if (long) SnackbarDuration.Long else SnackbarDuration.Short,
                )
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, showSnackbar) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    SnackbarManager.getEnqueuedSnackbars().forEach {
                        showSnackbar(it.message, it.long)
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    EventHandler { event ->
        when (event) {
            is ViewModelEvent.ShowSnackbar -> if (activity?.isFinishing == true) {
                false
            } else {
                consume {
                    showSnackbar(event.message.localize(context).toString(), event.long)
                }
            }
            else -> false
        }
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility),
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clearAndSetSemantics {
                                text = buildAnnotatedString {
                                    append(title)
                                    if (subtitle != null) {
                                        append(": ")
                                        append(subtitle)
                                    }
                                }
                            }
                            .runIf(onTitleClicked != null) {
                                clickable(onClick = onTitleClicked!!)
                            },
                    ) {
                        Text(
                            // due to some bug(?) in Compose (I guess?), I need to transform the title to trigger a recomposition
                            title + "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                fontSize = FontSize.SMALL,
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (backButton == null) {
                        return@TopAppBar
                    }
                    IconButton(
                        onClick = {
                            activity?.onBackPressed()
                        },
                    ) {
                        Icon(
                            when (backButton) {
                                BackButton.ARROW -> Icons.AutoMirrored.Filled.ArrowBack
                                BackButton.CROSS -> Icons.Filled.Close
                            },
                            contentDescription = when (backButton) {
                                BackButton.ARROW -> stringResource(R.string.accessibility_label_go_back)
                                BackButton.CROSS -> stringResource(R.string.accessibility_label_close)
                            },
                        )
                    }
                },
                actions = {
                    if (viewState != null) {
                        actions(viewState)
                    }
                },
            )
        },
        floatingActionButton = {
            if (viewState != null) {
                floatingActionButton()
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        if (viewState != null) {
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
            ) {
                content(viewState)
            }
        } else {
            LoadingIndicator(
                modifier = Modifier.padding(contentPadding),
            )
        }
    }
}
