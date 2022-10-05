package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO
import org.json.JSONObject
import org.liquidplayer.javascript.JSValue

class SendMQTTMessagesActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO): SendMQTTMessagesAction {
        val options = if (actionDTO.argCount >= 3) {
            actionDTO.getObject(1)
        } else {
            null
        }
        val messages = if (actionDTO.argCount >= 3) {
            actionDTO.getList(2)
        } else {
            actionDTO.getList(1)
        }
            .orEmpty()
            .mapNotNull {
                when (it) {
                    is JSValue -> {
                        val obj = JSONObject(it.toJSON())
                        SendMQTTMessagesAction.Message(
                            topic = obj.getString("topic"),
                            payload = obj.getString("payload").toByteArray(),
                        )
                    }
                    else -> null
                }
            }

        return SendMQTTMessagesAction(
            serverUri = actionDTO.getString(0) ?: "",
            username = options?.get("username") as? String,
            password = options?.get("password") as? String,
            messages = messages,
        )
    }

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("sendMQTTMessage", "sendMqttMessages", "sendMqttMessage"),
        parameters = 3,
    )

    companion object {
        private const val TYPE = "send_mqtt_messages"
        private const val FUNCTION_NAME = "sendMQTTMessages"
    }
}
