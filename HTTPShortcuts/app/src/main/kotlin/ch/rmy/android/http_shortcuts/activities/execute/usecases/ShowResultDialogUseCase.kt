package ch.rmy.android.http_shortcuts.activities.execute.usecases

import android.content.Context
import android.content.Intent
import android.net.Uri
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope

class ShowResultDialogUseCase
@Inject
constructor(
    private val context: Context,
    private val activityProvider: ActivityProvider,
    private val clipboardUtil: ClipboardUtil,
    private val shareUtil: ShareUtil,
    private val executionStarter: ExecutionStarter,
) {

    suspend operator fun invoke(shortcut: Shortcut, response: ShortcutResponse?, output: String?, dialogHandle: DialogHandle) = coroutineScope {
        val shortcutName = shortcut.getSafeName(context)
        val text = output ?: response?.getContentAsString(context) ?: ""
        val action = shortcut.responseDisplayActions.firstOrNull()
            ?.takeIf {
                when (it) {
                    ResponseDisplayAction.RERUN -> true
                    ResponseDisplayAction.SHARE -> text.isNotEmpty() && text.length < MAX_SHARE_LENGTH
                    ResponseDisplayAction.COPY -> text.isNotEmpty() && text.length < MAX_COPY_LENGTH
                    ResponseDisplayAction.SAVE -> false
                }
            }
        try {
            dialogHandle.showDialog(
                ExecuteDialogState.ShowResult(
                    title = shortcutName,
                    action = action,
                    content = if (output == null && shortcut.responseContentType == null && FileTypeUtil.isImage(response?.contentType)) {
                        ExecuteDialogState.ShowResult.Content.Image(
                            response!!.getContentUri(context)!!,
                        )
                    } else {
                        ExecuteDialogState.ShowResult.Content.Text(
                            text = (output ?: response?.getContentAsString(context) ?: "")
                                .ifBlank { context.getString(R.string.message_blank_response) },
                            allowHtml = shortcut.responseContentType == ResponseContentType.HTML,
                        )
                    },
                    monospace = shortcut.responseMonospace,
                    fontSize = shortcut.responseFontSize,
                ),
            )
        } catch (e: DialogCancellationException) {
            return@coroutineScope
        }

        when (action) {
            ResponseDisplayAction.RERUN -> {
                rerunShortcut(shortcut.id)
            }
            ResponseDisplayAction.SHARE -> {
                shareResponse(shortcutName, text, response?.contentType ?: "", response?.getContentUri(context))
            }
            ResponseDisplayAction.COPY -> {
                copyResponse(text)
            }
            else -> Unit
        }
    }

    private fun rerunShortcut(shortcutId: ShortcutId) {
        executionStarter.execute(
            shortcutId = shortcutId,
            trigger = ShortcutTriggerType.DIALOG_RERUN,
        )
    }

    private suspend fun shareResponse(shortcutName: String, text: String, type: String, responseFileUri: Uri?) {
        activityProvider.withActivity { activity ->
            if (shouldShareAsText(text, type)) {
                shareUtil.shareText(activity, text)
            } else {
                Intent(Intent.ACTION_SEND)
                    .setType(type)
                    .putExtra(Intent.EXTRA_STREAM, responseFileUri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .let {
                        Intent.createChooser(it, shortcutName)
                    }
                    .startActivity(activity)
            }
        }
    }

    private fun shouldShareAsText(text: String, type: String) =
        !FileTypeUtil.isImage(type) && text.length < MAX_SHARE_LENGTH

    private fun copyResponse(text: String) {
        clipboardUtil.copyToClipboard(text)
    }

    companion object {
        private const val MAX_SHARE_LENGTH = 300000
        private const val MAX_COPY_LENGTH = 300000
    }
}
