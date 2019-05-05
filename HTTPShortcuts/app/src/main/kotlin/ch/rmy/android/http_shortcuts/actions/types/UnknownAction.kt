package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class UnknownAction(actionType: UnknownActionType, data: Map<String, String>) : BaseAction(actionType, data) {

    override fun getDescription(context: Context): String = context.getString(R.string.action_type_unknown_description)

}