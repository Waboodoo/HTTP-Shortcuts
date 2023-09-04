package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import android.content.Intent
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CodeSnippetPickerActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        CodeSnippetPickerScreen(
            currentShortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID),
            includeResponseOptions = intent.getBooleanExtra(EXTRA_INCLUDE_RESPONSE_OPTIONS, false),
            includeNetworkErrorOption = intent.getBooleanExtra(EXTRA_INCLUDE_NETWORK_ERROR_OPTION, false),
        )
    }

    object PickCodeSnippet : BaseActivityResultContract<IntentBuilder, PickCodeSnippet.Result?>(::IntentBuilder) {
        override fun parseResult(resultCode: Int, intent: Intent?): Result? {
            return Result(
                textBeforeCursor = intent?.getStringExtra(EXTRA_TEXT_BEFORE_CURSOR) ?: return null,
                textAfterCursor = intent.getStringExtra(EXTRA_TEXT_AFTER_CURSOR) ?: return null,
            )
        }

        fun createResult(textBeforeCursor: String, textAfterCursor: String) =
            createIntent {
                putExtra(EXTRA_TEXT_BEFORE_CURSOR, textBeforeCursor)
                putExtra(EXTRA_TEXT_AFTER_CURSOR, textAfterCursor)
            }

        private const val EXTRA_TEXT_BEFORE_CURSOR = "text_before_cursor"
        private const val EXTRA_TEXT_AFTER_CURSOR = "text_after_cursor"

        data class Result(val textBeforeCursor: String, val textAfterCursor: String)
    }

    class IntentBuilder : BaseIntentBuilder(CodeSnippetPickerActivity::class) {

        fun currentShortcutId(shortcutId: ShortcutId) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }

        fun includeResponseOptions(includeResponseOptions: Boolean) = also {
            intent.putExtra(EXTRA_INCLUDE_RESPONSE_OPTIONS, includeResponseOptions)
        }

        fun includeNetworkErrorOption(includeNetworkErrorOption: Boolean) = also {
            intent.putExtra(EXTRA_INCLUDE_NETWORK_ERROR_OPTION, includeNetworkErrorOption)
        }
    }

    companion object {

        private const val EXTRA_SHORTCUT_ID = "shortcutId"
        private const val EXTRA_INCLUDE_RESPONSE_OPTIONS = "include_response_options"
        private const val EXTRA_INCLUDE_NETWORK_ERROR_OPTION = "include_network_error_option"
    }
}
