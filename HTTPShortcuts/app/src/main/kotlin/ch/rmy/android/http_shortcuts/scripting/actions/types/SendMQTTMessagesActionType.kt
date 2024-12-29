package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class SendMQTTMessagesActionType
@Inject
constructor(
    private val sendMQTTMessagesAction: SendMQTTMessagesAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs): ActionRunnable<*> {
        var optionsAvailable = true
        val messages = (
            args.getListOfObjects(2)
                ?: run {
                    optionsAvailable = false
                    args.getListOfObjects(1)
                }
            )
            .orEmpty()
            .mapNotNull { obj ->
                SendMQTTMessagesAction.Message(
                    topic = obj["topic"]?.toString() ?: return@mapNotNull null,
                    payload = obj["payload"]?.toString()?.toByteArray() ?: return@mapNotNull null,
                )
            }
        val options = if (optionsAvailable) {
            args.getObject(1)
        } else {
            null
        }

        return ActionRunnable(
            action = sendMQTTMessagesAction,
            params = SendMQTTMessagesAction.Params(
                serverUri = args.getString(0) ?: "",
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
