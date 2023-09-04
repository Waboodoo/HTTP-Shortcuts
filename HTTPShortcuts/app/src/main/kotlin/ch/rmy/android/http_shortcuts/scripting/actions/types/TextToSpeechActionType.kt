package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class TextToSpeechActionType
@Inject
constructor(
    private val textToSpeechAction: TextToSpeechAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = textToSpeechAction,
            params = TextToSpeechAction.Params(
                message = actionDTO.getString(0) ?: "",
                language = actionDTO.getString(1) ?: "",
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "text_to_speech"
        private const val FUNCTION_NAME = "speak"
    }
}
