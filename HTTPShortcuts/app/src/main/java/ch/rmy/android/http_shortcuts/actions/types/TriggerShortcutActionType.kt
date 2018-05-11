package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class TriggerShortcutActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_trigger_shortcut_title)

    override fun fromDTO(actionDTO: ActionDTO) = TriggerShortcutAction(actionDTO.id, this, actionDTO.data)

    override val isValidBeforeAction = false

    companion object {

        const val TYPE = "trigger_shortcut"

    }

}