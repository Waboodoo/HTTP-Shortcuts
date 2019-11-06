package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO

@Deprecated("Will be removed eventually")
class ExtractCookieActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = "Extract Cookie"

    override fun fromDTO(actionDTO: ActionDTO) = ExtractCookieAction(this, actionDTO.data)

    companion object {

        const val TYPE = "extract_cookie"

    }

}