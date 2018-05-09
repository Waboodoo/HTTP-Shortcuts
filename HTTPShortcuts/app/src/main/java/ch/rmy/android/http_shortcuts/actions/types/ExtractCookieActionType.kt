package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class ExtractCookieActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_extract_cookie_title)

    override val isValidBeforeAction = false

    override fun fromDTO(actionDTO: ActionDTO) = ExtractCookieAction(actionDTO.id, this, actionDTO.data)

    companion object {

        const val TYPE = "extract_cookie"

    }

}