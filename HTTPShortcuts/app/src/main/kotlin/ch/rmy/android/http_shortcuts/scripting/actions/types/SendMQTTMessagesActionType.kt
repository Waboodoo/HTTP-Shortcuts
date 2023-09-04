package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import org.json.JSONObject
import org.liquidplayer.javascript.JSValue
import javax.inject.Inject

class SendMQTTMessagesActionType
@Inject
constructor(
    private val sendMQTTMessagesAction: SendMQTTMessagesAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData): ActionRunnable<*> {
        var optionsAvailable = true
        val messages = (
            actionDTO.getList(2)
                ?: run {
                    optionsAvailable = false
                    actionDTO.getList(1)
                }
            )
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
        val options = if (optionsAvailable) {
            actionDTO.getObject(1)
        } else {
            null
        }

        return ActionRunnable(
            action = sendMQTTMessagesAction,
            params = SendMQTTMessagesAction.Params(
                serverUri = actionDTO.getString(0) ?: "",
                username = options?.get("username") as? String,
                password = options?.get("password") as? String,
                messages = messages,
            ),
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
