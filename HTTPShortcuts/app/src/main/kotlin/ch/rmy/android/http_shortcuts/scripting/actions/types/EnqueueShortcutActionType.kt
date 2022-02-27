package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO
import kotlin.math.min

class EnqueueShortcutActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = EnqueueShortcutAction(
        shortcutNameOrId = actionDTO.getString(KEY_SHORTCUT_NAME_OR_ID)?.takeUnlessEmpty(),
        variableValues = actionDTO.getObject(KEY_VARIABLE_VALUES),
        delay = actionDTO.getInt(KEY_DELAY)?.takeUnless { it < 0 }?.let { min(it, MAX_DELAY) },
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(
            KEY_SHORTCUT_NAME_OR_ID,
            KEY_VARIABLE_VALUES,
            KEY_DELAY,
        ),
        functionNameAliases = setOf("triggerShortcut"),
    )

    companion object {

        const val TYPE = "enqueue_shortcut"
        const val FUNCTION_NAME = "enqueueShortcut"

        const val KEY_SHORTCUT_NAME_OR_ID = "shortcutId"
        const val KEY_VARIABLE_VALUES = "variables"
        const val KEY_DELAY = "delay"

        private const val MAX_DELAY = 5 * 60 * 60 * 1000
    }
}
