package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class TriggerShortcutActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = TriggerShortcutAction(
        shortcutNameOrId = actionDTO[KEY_SHORTCUT_NAME_OR_ID] ?: "",
        variableValuesJson = actionDTO[KEY_VARIABLE_VALUES] ?: ""
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(
            KEY_SHORTCUT_NAME_OR_ID,
            KEY_VARIABLE_VALUES
        )
    )

    companion object {

        const val TYPE = "trigger_shortcut"
        const val FUNCTION_NAME = "triggerShortcut"

        const val KEY_SHORTCUT_NAME_OR_ID = "shortcutId"
        const val KEY_VARIABLE_VALUES = "variables"

    }

}