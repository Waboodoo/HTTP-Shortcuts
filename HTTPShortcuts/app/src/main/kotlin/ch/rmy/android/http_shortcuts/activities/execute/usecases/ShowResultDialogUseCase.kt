package ch.rmy.android.http_shortcuts.activities.execute.usecases

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.widget.ImageView
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.databinding.DialogTextBinding
import ch.rmy.android.http_shortcuts.extensions.getSafeName
import ch.rmy.android.http_shortcuts.extensions.loadImage
import ch.rmy.android.http_shortcuts.extensions.reloadImageSpans
import ch.rmy.android.http_shortcuts.extensions.showAndAwaitDismissal
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShowResultDialogUseCase
@Inject
constructor(
    private val context: Context,
    private val activityProvider: ActivityProvider,
    private val clipboardUtil: ClipboardUtil,
) {

    suspend operator fun invoke(shortcut: ShortcutModel, response: ShortcutResponse?, output: String?) = coroutineScope {
        val shortcutName = shortcut.getSafeName(context)
        withContext(Dispatchers.Main) {
            val activity = activityProvider.getActivity()
            DialogBuilder(activity)
                .title(shortcutName)
                .let { builder ->
                    if (output == null && FileTypeUtil.isImage(response?.contentType)) {
                        val imageView = ImageView(activity)
                        imageView.loadImage(response!!.contentFile!!, preventMemoryCache = true)
                        builder.view(imageView)
                    } else {
                        val view = DialogTextBinding.inflate(LayoutInflater.from(activity))
                        val textView = view.text
                        val finalOutput = (output ?: response?.getContentAsString(context) ?: "")
                            .ifBlank { context.getString(R.string.message_blank_response) }
                            .let {
                                HTMLUtil.formatWithImageSupport(it, context, textView::reloadImageSpans, this)
                            }
                        textView.text = finalOutput
                        textView.movementMethod = LinkMovementMethod.getInstance()
                        builder.view(textView)
                    }
                }
                .positive(R.string.dialog_ok)
                .runIfNotNull(shortcut.responseHandling?.displayActions?.firstOrNull()) { action ->
                    val text = output ?: response?.getContentAsString(context) ?: ""
                    when (action) {
                        ResponseDisplayAction.RERUN -> {
                            neutral(R.string.action_rerun_shortcut) {
                                rerunShortcut(shortcut.id)
                            }
                        }
                        ResponseDisplayAction.SHARE -> {
                            runIf(text.isNotEmpty() && text.length < MAX_SHARE_LENGTH) {
                                neutral(R.string.share_button) {
                                    shareResponse(shortcutName, text, response?.contentType ?: "", response?.contentFile)
                                }
                            }
                        }
                        ResponseDisplayAction.COPY -> {
                            runIf(text.isNotEmpty() && text.length < MAX_COPY_LENGTH) {
                                neutral(R.string.action_copy_response) {
                                    copyResponse(text)
                                }
                            }
                        }
                        ResponseDisplayAction.SAVE -> this
                    }
                }
                .showAndAwaitDismissal()
        }
    }

    private fun rerunShortcut(shortcutId: ShortcutId) {
        ExecuteActivity.IntentBuilder(shortcutId)
            .trigger(ShortcutTriggerType.DIALOG_RERUN)
            .startActivity(context)
    }

    private fun shareResponse(shortcutName: String, text: String, type: String, responseFileUri: Uri?) {
        if (shouldShareAsText(text, type)) {
            ShareUtil.shareText(activityProvider.getActivity(), text)
        } else {
            Intent(Intent.ACTION_SEND)
                .setType(type)
                .putExtra(Intent.EXTRA_STREAM, responseFileUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .let {
                    Intent.createChooser(it, shortcutName)
                }
                .startActivity(activityProvider.getActivity())
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
