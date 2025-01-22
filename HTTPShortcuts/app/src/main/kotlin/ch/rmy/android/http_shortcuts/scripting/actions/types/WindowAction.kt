package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.activities.response.DisplayResponseActivity
import ch.rmy.android.http_shortcuts.activities.response.models.ResponseData
import ch.rmy.android.http_shortcuts.navigation.NavigationArgStore
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class WindowAction
@Inject
constructor(
    private val context: Context,
    private val navigationArgStore: NavigationArgStore,
) : Action<WindowAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val title = config["title"]?.toString() ?: ""
        val text = config["text"]?.toString()?.takeUnlessEmpty() ?: return
        val mimeType = config["mimeType"]?.toString()
        val monospace = config["monospace"]?.toString()?.toBoolean() == true
        val fontSize = config["fontSize"]?.toString()?.toIntOrNull()?.coerceIn(5, 50)
        val responseData = ResponseData(
            shortcutId = executionContext.shortcutId,
            title = title,
            text = text,
            mimeType = mimeType,
            monospace = monospace,
            fontSize = fontSize,
        )
        val responseDataId = navigationArgStore.storeArg(responseData)
        DisplayResponseActivity.IntentBuilder(title, responseDataId)
            .startActivity(context)
    }

    data class Params(
        val config: Map<String, Any?>,
    )
}
