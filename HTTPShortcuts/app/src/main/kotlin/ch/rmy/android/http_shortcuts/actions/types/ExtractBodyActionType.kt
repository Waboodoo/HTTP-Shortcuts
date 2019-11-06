package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO

@Deprecated("Will be removed eventually")
class ExtractBodyActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = "Extract from Body"

    override fun fromDTO(actionDTO: ActionDTO) = ExtractBodyAction(this, actionDTO.data)

    companion object {

        const val TYPE = "extract_body"

    }

}