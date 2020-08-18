package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class ResponseHandling(
    @PrimaryKey
    @Required
    override var id: String = UUIDUtils.newUUID(),
    var uiType: String = UI_TYPE_TOAST,
    var successOutput: String = SUCCESS_OUTPUT_RESPONSE,
    var failureOutput: String = FAILURE_OUTPUT_DETAILED,
    var successMessage: String = "",
    var includeMetaInfo: Boolean = false,
) : RealmObject(), HasId {

    fun validate() {
        if (!UUIDUtils.isUUID(id) && id.toIntOrNull() == null) {
            throw IllegalArgumentException("Invalid response handling ID found, must be UUID: $id")
        }

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

    fun isSameAs(other: ResponseHandling?) =
        other?.uiType == uiType
            && other.successOutput == successOutput
            && other.failureOutput == failureOutput
            && other.successMessage == successMessage
            && other.includeMetaInfo == includeMetaInfo

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