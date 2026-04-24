package ch.rmy.android.http_shortcuts.activities.main

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.Application
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.RealmMigrator
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.applock.AppLockController
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.ImportMode
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.NavigationRoot
import ch.rmy.android.http_shortcuts.utils.ActivityCloser
import ch.rmy.android.http_shortcuts.utils.IntegrationUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : BaseComposeActivity() {

    @Inject
    lateinit var importer: Importer

    @Inject
    lateinit var integrationUtil: IntegrationUtil

    @Inject
    lateinit var appLockController: AppLockController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appLockController.monitor()
            }
        }
    }

    @Composable
    override fun Content() {
        val navHostController = rememberNavController()

        LaunchedEffect(Unit) {
            appLockController.observeLocked()
                .collect { isLocked ->
                    if (isLocked && navHostController.currentDestination?.route != NavigationDestination.Main.routePattern) {
                        navHostController.navigate(NavigationDestination.Main.routePattern) {
                            popUpTo(navHostController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                }
        }

        if (Application.unmigratedRealmFound) {
            var showDeletionWarning by remember { mutableStateOf(false) }

            val openFilePickerForImport = rememberLauncherForActivityResult(FilePickerUtil.PickFile) { fileUri ->
                fileUri?.let {
                    lifecycleScope.launch {
                        try {
                            logInfo("Starting restore from import")
                            importer.importFromUri(it, importMode = ImportMode.MERGE)
                            RealmMigrator.setMigrated(context)
                            showToast(R.string.message_data_restored)
                            recreate()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            if (e !is ImportException) {
                                logException(e)
                            }
                            showToast(context.getString(R.string.import_failed_with_reason, e.message ?: e::class.java.simpleName))
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                val importUri = intent.getParcelable<Uri>(Intent.EXTRA_STREAM)
                importUri?.let {
                    lifecycleScope.launch {
                        try {
                            logInfo("Starting restore from Restore app")
                            importer.importFromUri(it, importMode = ImportMode.MERGE)
                            RealmMigrator.setMigrated(context)
                            showToast(R.string.message_data_restored)
                            recreate()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            if (e !is ImportException) {
                                logException(e)
                            }
                            showToast(context.getString(R.string.import_failed_with_reason, e.message ?: e::class.java.simpleName))
                        }
                    }
                }
            }

            var isBusy by remember {
                mutableStateOf(false)
            }
            val coroutineScope = rememberCoroutineScope()
            fun runAction(action: suspend () -> Unit) {
                coroutineScope.launch {
                    if (!isBusy) {
                        isBusy = true
                        action()
                        try {
                        } finally {
                            isBusy = false
                        }
                    }
                }
            }

            if (showDeletionWarning) {
                DeletionWarning(
                    onConfirm = {
                        RealmMigrator.deleteRealmFile(context)
                        recreate()
                    },
                    onDismissed = {
                        finishWithoutAnimation()
                    },
                )
                return
            }

            if (isBusy) {
                ProgressDialog(onDismissRequest = {})
            }

            UnmigratedRealmError(
                showRestoreButton = integrationUtil.isRestoreAppAvailable(),
                onRestoreClicked = {
                    runAction {
                        invokeRestoreApp()
                    }
                },
                onExportClicked = {
                    runAction {
                        exportRealmFile()
                    }
                },
                onImportClicked = {
                    openFilePickerForImport.launch(null)
                },
                onDeleteClicked = {
                    showDeletionWarning = true
                },
                onDismissed = {
                    finishWithoutAnimation()
                },
            )

            return
        }

        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxSize(),
        ) {
            NavigationRoot(navHostController)
        }
    }

    @Composable
    private fun UnmigratedRealmError(
        showRestoreButton: Boolean,
        onRestoreClicked: () -> Unit,
        onExportClicked: () -> Unit,
        onImportClicked: () -> Unit,
        onDeleteClicked: () -> Unit,
        onDismissed: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismissed,
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.error_manual_data_migration, "http-shortcuts.rmy.ch/restore"))

                    VerticalSpacer(height = Spacing.MEDIUM)

                    if (showRestoreButton) {
                        Button(
                            onClick = onRestoreClicked,
                        ) {
                            Text(stringResource(R.string.button_restore_data))
                        }
                    }

                    VerticalSpacer(height = Spacing.MEDIUM)

                    Button(
                        onClick = onExportClicked,
                    ) {
                        Text(stringResource(R.string.button_export_data))
                    }
                    Button(
                        onClick = onImportClicked,
                    ) {
                        Text(stringResource(R.string.button_import_data))
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.warning),
                        ),
                        onClick = onDeleteClicked,
                    ) {
                        Text(stringResource(R.string.button_delete_data))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onDismissed,
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }

    @Composable
    private fun DeletionWarning(
        onConfirm: () -> Unit,
        onDismissed: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismissed,
            title = { Text(stringResource(R.string.warning_dialog_title)) },
            text = {
                Text(stringResource(R.string.message_delete_all))
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                ) {
                    Text(stringResource(R.string.dialog_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissed,
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }

    private suspend fun invokeRestoreApp() {
        val zipFile = withContext(Dispatchers.IO) {
            RealmMigrator.getShareableRealmExportFile(context)
        }
        Intent(Intent.ACTION_SEND)
            .setClassName(
                "ch.rmy.android.http_shortcuts.restore",
                "ch.rmy.android.http_shortcuts.activities.main.MainActivity",
            )
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setType("application/zip")
            .putExtra(Intent.EXTRA_STREAM, zipFile)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .startActivity(this)
        finish()
    }

    private suspend fun exportRealmFile() {
        val zipFile = withContext(Dispatchers.IO) {
            RealmMigrator.getShareableRealmExportFile(context)
        }
        Intent(Intent.ACTION_SEND)
            .setType("application/zip")
            .putExtra(Intent.EXTRA_STREAM, zipFile)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .let {
                Intent.createChooser(it, getString(R.string.button_export_data))
            }
            .startActivity(this@MainActivity)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is MainEvent.Restart -> {
                finish()
                startActivity(
                    IntentBuilder()
                        .categoryId(event.activeCategoryId)
                        .build(context),
                )
            }
            else -> super.handleEvent(event)
        }
    }

    override fun onStart() {
        super.onStart()
        if (ActivityCloser.shouldCloseMainActivity()) {
            finishWithoutAnimation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCloser.onMainActivityDestroyed()
    }

    object SelectShortcut : ActivityResultContract<Unit, SelectShortcut.Result?>() {
        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(context, MainActivity::class.java)
                .setAction(ACTION_SELECT_SHORTCUT_FOR_PLUGIN)

        override fun parseResult(resultCode: Int, intent: Intent?): Result? =
            if (resultCode == RESULT_OK && intent != null) {
                Result(
                    shortcutId = intent.getStringExtra(EXTRA_SELECTION_ID)!!,
                    shortcutName = intent.getStringExtra(EXTRA_SELECTION_NAME)!!,
                )
            } else {
                null
            }

        data class Result(val shortcutId: ShortcutId, val shortcutName: String)
    }

    class IntentBuilder : BaseIntentBuilder(MainActivity::class) {
        init {
            intent.action = Intent.ACTION_VIEW
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        fun categoryId(categoryId: CategoryId) = also {
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
        }

        fun widgetId(widgetId: Int?) = also {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }

        fun importUrl(importUrl: Uri) = also {
            intent.putExtra(EXTRA_IMPORT_URL, importUrl)
        }

        fun cancelPendingExecutions() = also {
            intent.putExtra(EXTRA_CANCEL_PENDING_EXECUTIONS, true)
        }
    }

    companion object {

        const val ACTION_SELECT_SHORTCUT_FOR_PLUGIN = "ch.rmy.android.http_shortcuts.plugin"

        const val EXTRA_SELECTION_ID = "ch.rmy.android.http_shortcuts.shortcut_id"
        const val EXTRA_SELECTION_NAME = "ch.rmy.android.http_shortcuts.shortcut_name"
        const val EXTRA_CATEGORY_ID = "ch.rmy.android.http_shortcuts.category_id"
        const val EXTRA_IMPORT_URL = "ch.rmy.android.http_shortcuts.import_url"
        const val EXTRA_CANCEL_PENDING_EXECUTIONS = "ch.rmy.android.http_shortcuts.cancel_executions"

        fun determineMode(context: Context, intent: Intent) = when (intent.action) {
            Intent.ACTION_CREATE_SHORTCUT -> SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT
            AppWidgetManager.ACTION_APPWIDGET_CONFIGURE -> {
                val variableLayout = AppWidgetManager.getInstance(context)
                    .getAppWidgetInfo(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0))
                    ?.initialLayout
                when (variableLayout) {
                    R.layout.variable_widget -> SelectionMode.VARIABLE_WIDGET_PLACEMENT
                    R.layout.shortcut_widget -> SelectionMode.SHORTCUT_WIDGET_PLACEMENT
                    else -> SelectionMode.SHORTCUT_WIDGET_PLACEMENT // TODO: Find a better fallback
                }
            }
            ACTION_SELECT_SHORTCUT_FOR_PLUGIN -> SelectionMode.PLUGIN
            else -> SelectionMode.NORMAL
        }
    }
}
