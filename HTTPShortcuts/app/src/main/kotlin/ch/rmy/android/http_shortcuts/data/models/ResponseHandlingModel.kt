package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.RealmClass

@RealmClass(name = "ResponseHandling", embedded = true)
open class ResponseHandlingModel(
    var uiType: String = UI_TYPE_WINDOW,
    var successOutput: String = SUCCESS_OUTPUT_RESPONSE,
    var failureOutput: String = FAILURE_OUTPUT_DETAILED,
    var successMessage: String = "",
    var includeMetaInfo: Boolean = false,
    displayActions: List<ResponseDisplayAction> = listOf(
        ResponseDisplayAction.RERUN,
        ResponseDisplayAction.SHARE,
        ResponseDisplayAction.SAVE,
    ),
) : RealmModel {

    private var actions: RealmList<String>

    init {
        actions = RealmList<String>().apply {
            addAll(displayActions.map { it.key })
        }
    }

    var displayActions: List<ResponseDisplayAction>
        get() = actions.mapNotNull(ResponseDisplayAction::parse)
        set(value) {
            actions = RealmList<String>().apply {
                addAll(value.map { it.key })
            }
        }

    fun validate() {
        if (uiType !in setOf(UI_TYPE_WINDOW, UI_TYPE_DIALOG, UI_TYPE_TOAST)) {
            throw IllegalArgumentException("Invalid response handling type: $uiType")
        }

        if (successOutput !in setOf(SUCCESS_OUTPUT_MESSAGE, SUCCESS_OUTPUT_RESPONSE, SUCCESS_OUTPUT_NONE)) {
            throw IllegalArgumentException("Invalid response handling success output: $successOutput")
        }

        if (failureOutput !in setOf(FAILURE_OUTPUT_DETAILED, FAILURE_OUTPUT_SIMPLE, FAILURE_OUTPUT_NONE)) {
            throw IllegalArgumentException("Invalid response handling failure output: $failureOutput")
        }
    }

    fun isSameAs(other: ResponseHandlingModel?) =
        other?.uiType == uiType &&
            other.successOutput == successOutput &&
            other.failureOutput == failureOutput &&
            other.successMessage == successMessage &&
            other.includeMetaInfo == includeMetaInfo &&
            other.actions == actions

    companion object {

        const val UI_TYPE_TOAST = "toast"
        const val UI_TYPE_DIALOG = "dialog"
        const val UI_TYPE_WINDOW = "window"

        const val SUCCESS_OUTPUT_RESPONSE = "response"
        const val SUCCESS_OUTPUT_MESSAGE = "message"
        const val SUCCESS_OUTPUT_NONE = "none"

        const val FAILURE_OUTPUT_DETAILED = "detailed"
        const val FAILURE_OUTPUT_SIMPLE = "simple"
        const val FAILURE_OUTPUT_NONE = "none"
    }
}
