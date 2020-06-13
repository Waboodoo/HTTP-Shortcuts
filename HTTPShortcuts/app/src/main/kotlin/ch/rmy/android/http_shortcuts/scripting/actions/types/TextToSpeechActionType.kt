package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class TextToSpeechActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = TextToSpeechAction(actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(TextToSpeechAction.KEY_TEXT, TextToSpeechAction.KEY_LANGUAGE)
    )

    companion object {

        const val TYPE = "text_to_speech"
        const val FUNCTION_NAME = "speak"

    }

}