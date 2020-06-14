package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class TextToSpeechActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = TextToSpeechAction(
        message = actionDTO[KEY_TEXT] ?: "",
        language = actionDTO[KEY_LANGUAGE] ?: ""
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_TEXT, KEY_LANGUAGE)
    )

    companion object {

        const val TYPE = "text_to_speech"
        const val FUNCTION_NAME = "speak"

        const val KEY_TEXT = "text"
        const val KEY_LANGUAGE = "language"

    }

}