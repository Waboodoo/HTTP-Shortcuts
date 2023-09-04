package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class ExecuteShortcutActionType
@Inject
constructor(
    private val executeShortcutAction: ExecuteShortcutAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = executeShortcutAction,
            params = ExecuteShortcutAction.Params(
                shortcutNameOrId = actionDTO.getString(0)?.takeUnlessEmpty(),
                variableValues = actionDTO.getObject(1),
            )
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "execute_shortcut"
        private const val FUNCTION_NAME = "executeShortcut"
    }
}
