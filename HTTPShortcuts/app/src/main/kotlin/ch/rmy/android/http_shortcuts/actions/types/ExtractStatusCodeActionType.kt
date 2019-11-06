package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO

@Deprecated("Will be removed eventually")
class ExtractStatusCodeActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = "Extract Status Code"

    override fun fromDTO(actionDTO: ActionDTO) = ExtractStatusCodeAction(this, actionDTO.data)

    companion object {

        const val TYPE = "extract_status_code"

    }

}