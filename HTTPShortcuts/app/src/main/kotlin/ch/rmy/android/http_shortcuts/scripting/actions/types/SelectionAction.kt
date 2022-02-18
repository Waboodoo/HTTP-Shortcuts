package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.mapFor
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.exceptions.JavaScriptException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class SelectionAction(
    private val dataObject: Map<String, Any?>?,
    private val dataList: List<Any?>?,
) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> {
        val options = parseData(dataObject, dataList)
        return if (options.isNotEmpty()) {
            Single.create<String> { emitter ->
                DialogBuilder(executionContext.context)
                    .mapFor(options.entries) { entry ->
                        item(name = entry.value) {
                            emitter.onSuccess("-${entry.key}")
                        }
                    }
                    .dismissListener { emitter.onSuccess("") }
                    .show()
            }
                .subscribeOn(AndroidSchedulers.mainThread())
                .map {
                    it.takeUnlessEmpty()
                        ?.removePrefix("-")
                        ?: NO_RESULT
                }
        } else {
            Single.just(NO_RESULT)
        }
    }

    private fun parseData(dataObject: Map<String, Any?>?, dataList: List<Any?>?): Map<String, String> =
        dataObject?.mapValues { it.value?.toString() ?: "" }
            ?: dataList?.map { it?.toString() ?: "" }?.associateWith { it }
            ?: throw JavaScriptException("showSelection function expects object or array as argument")
}
