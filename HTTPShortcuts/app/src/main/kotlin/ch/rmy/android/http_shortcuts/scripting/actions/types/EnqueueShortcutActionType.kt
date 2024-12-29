package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class EnqueueShortcutActionType
@Inject
constructor(
    private val enqueueShortcutAction: EnqueueShortcutAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = enqueueShortcutAction,
            params = EnqueueShortcutAction.Params(
                shortcutNameOrId = args.getString(0)?.takeUnlessEmpty(),
                variableValues = args.getObject(1),
                delay = args.getInt(2)?.coerceIn(0, MAX_DELAY),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("triggerShortcut"),
        parameters = 3,
    )

    companion object {
        private const val TYPE = "enqueue_shortcut"
        private const val FUNCTION_NAME = "enqueueShortcut"

        private const val MAX_DELAY = 5 * 60 * 60 * 1000
    }
}
