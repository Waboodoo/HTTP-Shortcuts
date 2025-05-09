package ch.rmy.android.http_shortcuts.utils

import android.app.Activity
import android.content.Intent
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType.CAMERA
import ch.rmy.android.http_shortcuts.data.enums.ParameterType.FILE
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.getGlobalVariables
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import javax.inject.Inject

class ShareUtil
@Inject
constructor() {

    fun shareText(activity: Activity, text: String) {
        try {
            Intent(Intent.ACTION_SEND)
                .setType(TYPE_TEXT)
                .putExtra(Intent.EXTRA_TEXT, text)
                .let {
                    Intent.createChooser(it, activity.getString(R.string.share_title))
                        .startActivity(activity)
                }
        } catch (e: Exception) {
            activity.showToast(activity.getString(R.string.error_share_failed), long = true)
            logException(e)
        }
    }

    fun getTextShareGlobalVariables(variables: List<GlobalVariable>) =
        variables.filter { it.isShareText || it.isShareTitle }
            .toSet()

    fun isTextShareTarget(
        shortcut: Shortcut,
        headers: List<RequestHeader>,
        parameters: List<RequestParameter>,
        globalVariableIds: Set<GlobalVariableId>,
        variableManager: VariableManager,
    ): Boolean {
        val globalVariableIdsInShortcut = VariableResolver.findResolvableVariableIdentifiersIncludingScripting(
            shortcut,
            headers = headers,
            parameters = parameters,
        )
            .getGlobalVariables(variableManager)
        return globalVariableIds.any { globalVariableIdsInShortcut.contains(it) }
    }

    fun isFileShareTarget(shortcut: Shortcut, parameters: List<RequestParameter>, forImage: Boolean? = null): Boolean {
        if (shortcut.excludeFromFileSharing) {
            return false
        }
        return (parameters.any { it.parameterType == FILE && (it.fileUploadType != CAMERA || forImage != false) }) ||
            shortcut.usesGenericFileBody() ||
            (forImage != false && shortcut.fileUploadType == CAMERA)
    }

    companion object {
        private const val TYPE_TEXT = "text/plain"
    }
}
