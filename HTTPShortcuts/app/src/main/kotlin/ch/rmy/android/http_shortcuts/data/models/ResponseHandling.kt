package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.createRealmList
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class ResponseHandling(
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
            actions = createRealmList(value.map { it.key })
        }

    var storeDirectory: String? = null
    var storeFileName: String? = null

    fun validate() {
        require(uiType in setOf(UI_TYPE_WINDOW, UI_TYPE_DIALOG, UI_TYPE_TOAST)) {
            "Invalid response handling type: $uiType"
        }
        require(successOutput in setOf(SUCCESS_OUTPUT_MESSAGE, SUCCESS_OUTPUT_RESPONSE, SUCCESS_OUTPUT_NONE)) {
            "Invalid response handling success output: $successOutput"
        }
        require(failureOutput in setOf(FAILURE_OUTPUT_DETAILED, FAILURE_OUTPUT_SIMPLE, FAILURE_OUTPUT_NONE)) {
            "Invalid response handling failure output: $failureOutput"
        }
    }

    fun isSameAs(other: ResponseHandling?) =
        other?.uiType == uiType &&
            other.successOutput == successOutput &&
            other.failureOutput == failureOutput &&
            other.successMessage == successMessage &&
            other.includeMetaInfo == includeMetaInfo &&
            other.actions == actions &&
            other.storeDirectory == storeDirectory &&
            other.storeFileName == storeFileName

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
