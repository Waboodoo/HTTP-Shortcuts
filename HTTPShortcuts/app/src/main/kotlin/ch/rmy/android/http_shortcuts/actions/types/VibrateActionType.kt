package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.ActionAlias

class VibrateActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_vibrate_title)

    override fun fromDTO(actionDTO: ActionDTO) = VibrateAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "vibrate",
        parameters = listOf(VibrateAction.KEY_PATTERN, VibrateAction.KEY_WAIT_FOR_COMPLETION)
    )

    companion object {

        const val TYPE = "vibrate"

    }

}