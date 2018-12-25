package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.os.Vibrator
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class VibrateActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_vibrate_title)

    override val isAvailable = (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()

    override fun fromDTO(actionDTO: ActionDTO) = VibrateAction(actionDTO.id, this, actionDTO.data)

    companion object {

        const val TYPE = "vibrate"

    }

}