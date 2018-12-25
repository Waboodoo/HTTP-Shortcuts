package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class UnknownAction(id: String, actionType: UnknownActionType, data: Map<String, String>) : BaseAction(id, actionType, data) {

    override fun getDescription(context: Context): String = context.getString(R.string.action_type_unknown_description)

}