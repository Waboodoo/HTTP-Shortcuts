package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class ExtractStatusCodeActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_extract_status_code_title)

    override val isValidBeforeAction = false

    override fun fromDTO(actionDTO: ActionDTO) = ExtractStatusCodeAction(actionDTO.id, this, actionDTO.data)

    companion object {

        const val TYPE = "extract_status_code"

    }

}