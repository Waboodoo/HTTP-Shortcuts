package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class CancelShortcutActionType
@Inject
constructor(
    private val cancelShortcutAction: CancelShortcutAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = cancelShortcutAction,
            params = CancelShortcutAction.Params(
                shortcutNameOrId = args.getString(0)?.takeUnlessEmpty(),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 1,
    )

    companion object {
        private const val TYPE = "cancel_shortcut"
        private const val FUNCTION_NAME = "cancelShortcut"
    }
}
