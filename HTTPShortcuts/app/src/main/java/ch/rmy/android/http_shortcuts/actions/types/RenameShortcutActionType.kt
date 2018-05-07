package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager

class RenameShortcutActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_rename_shortcut_title)

    override fun fromDTO(actionDTO: ActionDTO) = RenameShortcutAction(actionDTO.id, this, actionDTO.data)

    override val isAvailable: Boolean
        get() = LauncherShortcutManager.supportsPinning(context)

    companion object {

        const val TYPE = "rename_shortcut"

    }

}