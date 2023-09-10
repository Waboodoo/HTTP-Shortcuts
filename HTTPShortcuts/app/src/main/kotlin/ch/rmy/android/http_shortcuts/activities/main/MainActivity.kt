package ch.rmy.android.http_shortcuts.activities.main

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.extensions.restartWithoutAnimation
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.data.RealmError
import ch.rmy.android.http_shortcuts.data.RealmFactoryImpl
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.navigation.NavigationRoot
import ch.rmy.android.http_shortcuts.utils.ActivityCloser
import ch.rmy.android.http_shortcuts.utils.ExternalURLs.RELEASES
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseComposeActivity() {

    override fun onCreated(savedState: Bundle?) {
        fixTabMinWidth()
        super.onCreated(savedState)
    }

    private fun fixTabMinWidth() {
        // I'm sorry for this evil, but the M3 library left me no other choice
        try {
            Class
                .forName("androidx.compose.material3.TabRowKt")
                .getDeclaredField("ScrollableTabRowMinimumTabWidth")
                .apply {
                    isAccessible = true
                }
                .set(this, 0f)
        } catch (e: Exception) {
            // If it fails, it fails
            logException(e)
        }
    }

    @Composable
    override fun Content() {
        RealmFactoryImpl.realmError?.let { error ->
            when (error) {
                is RealmError.ConfigurationError -> RealmConfigurationErrorDialog(
                    error.backupFile,
                ) {
                    finishWithoutAnimation()
                }
                is RealmError.RealmNotFound -> RealmUnavailableDialog {
                    finishWithoutAnimation()
                }
                is RealmError.Downgrade -> RealmDowngradeDialog {
                    finishWithoutAnimation()
                }
            }

            return
        }

        NavigationRoot()
    }

    @Composable
    private fun RealmConfigurationErrorDialog(
        backupFile: Uri,
        onDismissed: () -> Unit,
    ) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = onDismissed,
            title = { Text(stringResource(R.string.dialog_title_error)) },
            text = {
                Text(
                    "There was an unexpected issue while trying to load your data. Please contact the developer at android@rmy.ch.\n\n" +
                        "You can export a dump of your local database using the button below."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Intent(Intent.ACTION_SEND)
                            .setType("application/octet-stream")
                            .putExtra(Intent.EXTRA_STREAM, backupFile)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            .let {
                                Intent.createChooser(it, context.getString(R.string.title_export))
                            }
                            .startActivity(this)
                        onDismissed()
                    },
                ) {
                    Text(stringResource(R.string.dialog_button_export))
                }
            },
        )
    }

    @Composable
    private fun RealmUnavailableDialog(
        onDismissed: () -> Unit,
    ) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = onDismissed,
            title = { Text(stringResource(R.string.dialog_title_error)) },
            text = {
                val text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append(stringResource(R.string.error_realm_unavailable))
                    }
                    append(" ")
                    pushStringAnnotation(tag = "releases", annotation = RELEASES)
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(RELEASES)
                    }
                    pop()
                }
                ClickableText(
                    text,
                    onClick = { offset ->
                        text.getStringAnnotations(tag = "releases", start = offset, end = offset).firstOrNull()?.let {
                            context.openURL(it.item)
                        }
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissed) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    @Composable
    private fun RealmDowngradeDialog(
        onDismissed: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismissed,
            title = { Text(stringResource(R.string.dialog_title_error)) },
            text = {
                Text("It looks like you tried to downgrade the app. This is unfortunately not possible without clearing the app's data.")
            },
            confirmButton = {
                TextButton(onClick = onDismissed) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
        )
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            MainEvent.Restart -> {
                restartWithoutAnimation()
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
            if (resultCode == Activity.RESULT_OK && intent != null) {
                Result(
                    shortcutId = intent.getStringExtra(EXTRA_SELECTION_ID)!!,
                    shortcutName = intent.getStringExtra(EXTRA_SELECTION_NAME)!!,
                )
            } else null

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

        fun determineMode(action: String?) = when (action) {
            Intent.ACTION_CREATE_SHORTCUT -> SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT
            AppWidgetManager.ACTION_APPWIDGET_CONFIGURE -> SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT
            ACTION_SELECT_SHORTCUT_FOR_PLUGIN -> SelectionMode.PLUGIN
            else -> SelectionMode.NORMAL
        }
    }
}
