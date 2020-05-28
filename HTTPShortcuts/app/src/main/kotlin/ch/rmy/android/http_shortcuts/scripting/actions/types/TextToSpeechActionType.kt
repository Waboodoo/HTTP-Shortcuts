package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class TextToSpeechActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = TextToSpeechAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "speak",
        parameters = listOf(TextToSpeechAction.KEY_TEXT, TextToSpeechAction.KEY_LANGUAGE)
    )

    companion object {

        const val TYPE = "text_to_speech"

    }

}