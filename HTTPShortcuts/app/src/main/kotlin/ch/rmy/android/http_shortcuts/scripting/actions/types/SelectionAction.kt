package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.exceptions.JavaScriptException
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class SelectionAction(
    private val dataObject: Map<String, Any?>?,
    private val dataList: List<Any?>?,
) : BaseAction() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    override suspend fun execute(executionContext: ExecutionContext): String? {
        val options = parseData(dataObject, dataList)
        if (options.isEmpty()) {
            return null
        }

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<String> { continuation ->
                DialogBuilder(activityProvider.getActivity())
                    .runFor(options.entries) { entry ->
                        item(name = entry.value) {
                            continuation.resume("-${entry.key}")
                        }
                    }
                    .dismissListener {
                        if (continuation.isActive) {
                            continuation.resume("")
                        }
                    }
                    .showOrElse {
                        continuation.cancel()
                    }
            }
        }
            .takeUnlessEmpty()
            ?.removePrefix("-")
    }

    private fun parseData(dataObject: Map<String, Any?>?, dataList: List<Any?>?): Map<String, String> =
        dataObject?.mapValues { it.value?.toString() ?: "" }
            ?: dataList?.map { it?.toString() ?: "" }?.associateWith { it }
            ?: throw JavaScriptException("showSelection function expects object or array as argument")
}
