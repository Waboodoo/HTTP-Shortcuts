package ch.rmy.android.http_shortcuts.activities.editor

import android.content.Intent
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.getSerializable
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.curlcommand.CurlCommand
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShortcutEditorActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        ShortcutEditorScreen(
            categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID),
            shortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID),
            curlCommand = intent.getSerializable(EXTRA_CURL_COMMAND),
            executionType = intent.getStringExtra(EXTRA_EXECUTION_TYPE)
                ?.let(ShortcutExecutionType::get)
                ?: ShortcutExecutionType.APP,
            recoveryMode = intent.getBooleanExtra(EXTRA_RECOVERY_MODE, false),
        )
    }

    object OpenShortcutEditor : BaseActivityResultContract<IntentBuilder, String?>(::IntentBuilder) {

        private const val RESULT_SHORTCUT_ID = "shortcutId"

        override fun parseResult(resultCode: Int, intent: Intent?): ShortcutId? =
            intent?.getStringExtra(RESULT_SHORTCUT_ID)

        fun createResult(shortcutId: ShortcutId) =
            createIntent {
                putExtra(RESULT_SHORTCUT_ID, shortcutId)
            }
    }

    class IntentBuilder : BaseIntentBuilder(ShortcutEditorActivity::class) {

        fun shortcutId(shortcutId: ShortcutId) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }

        fun categoryId(categoryId: CategoryId) = also {
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
        }

        fun curlCommand(command: CurlCommand) = also {
            intent.putExtra(EXTRA_CURL_COMMAND, command)
        }

        fun executionType(type: ShortcutExecutionType) = also {
            intent.putExtra(EXTRA_EXECUTION_TYPE, type.type)
        }

        fun recoveryMode() = also {
            intent.putExtra(EXTRA_RECOVERY_MODE, true)
        }
    }

    companion object {

        private const val EXTRA_SHORTCUT_ID = "shortcutId"
        private const val EXTRA_CATEGORY_ID = "categoryId"
        private const val EXTRA_CURL_COMMAND = "curlCommand"
        private const val EXTRA_EXECUTION_TYPE = "executionType"
        private const val EXTRA_RECOVERY_MODE = "recoveryMode"
    }
}
