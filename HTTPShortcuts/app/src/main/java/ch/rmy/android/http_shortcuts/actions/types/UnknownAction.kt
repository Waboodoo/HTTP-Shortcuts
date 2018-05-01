package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class UnknownAction(actionDTO: ActionDTO) : BaseAction(actionDTO) {

    override fun getTitle(context: Context) = context.getString(R.string.action_type_unknown_title)

    override fun getDescription(context: Context) = context.getString(R.string.action_type_unknown_description)

}