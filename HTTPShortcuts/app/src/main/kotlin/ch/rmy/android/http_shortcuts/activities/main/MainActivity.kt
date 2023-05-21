package ch.rmy.android.http_shortcuts.activities.main

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.extensions.restartWithoutAnimation
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.activities.settings.SettingsActivity
import ch.rmy.android.http_shortcuts.data.RealmFactoryImpl
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import ch.rmy.android.http_shortcuts.utils.ActivityCloser
import ch.rmy.android.http_shortcuts.utils.ExternalURLs.RELEASES
import ch.rmy.android.http_shortcuts.widget.WidgetManager

class MainActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        if (!RealmFactoryImpl.isRealmAvailable) {
            RealmUnavailableDialog {
                finishWithoutAnimation()
            }
            return
        }

        MainScreen(
            selectionMode = determineMode(intent.action),
            initialCategoryId = intent?.extras?.getString(EXTRA_CATEGORY_ID),
            widgetId = WidgetManager.getWidgetIdFromIntent(intent),
            importUrl = intent?.getParcelable(EXTRA_IMPORT_URL),
            cancelPendingExecutions = intent?.extras?.getBoolean(EXTRA_CANCEL_PENDING_EXECUTIONS) ?: false,
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

    private fun determineMode(action: String?) = when (action) {
        Intent.ACTION_CREATE_SHORTCUT -> SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT
        AppWidgetManager.ACTION_APPWIDGET_CONFIGURE -> SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT
        ACTION_SELECT_SHORTCUT_FOR_PLUGIN -> SelectionMode.PLUGIN
        else -> SelectionMode.NORMAL
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            MainEvent.ReopenSettings -> {
                recreate()
                SettingsActivity.IntentBuilder()
                    .startActivity(this)
                overridePendingTransition(0, 0)
            }
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

        private const val ACTION_SELECT_SHORTCUT_FOR_PLUGIN = "ch.rmy.android.http_shortcuts.plugin"

        const val EXTRA_SELECTION_ID = "ch.rmy.android.http_shortcuts.shortcut_id"
        const val EXTRA_SELECTION_NAME = "ch.rmy.android.http_shortcuts.shortcut_name"
        private const val EXTRA_CATEGORY_ID = "ch.rmy.android.http_shortcuts.category_id"
        private const val EXTRA_IMPORT_URL = "ch.rmy.android.http_shortcuts.import_url"
        private const val EXTRA_CANCEL_PENDING_EXECUTIONS = "ch.rmy.android.http_shortcuts.cancel_executions"
    }
}
