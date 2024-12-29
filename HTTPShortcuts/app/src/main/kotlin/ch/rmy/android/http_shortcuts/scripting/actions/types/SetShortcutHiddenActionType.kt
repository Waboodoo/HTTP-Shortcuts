package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class SetShortcutHiddenActionType
@Inject
constructor(
    private val setShortcutHiddenAction: SetShortcutHiddenAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = setShortcutHiddenAction,
            params = SetShortcutHiddenAction.Params(
                shortcutNameOrId = args.getString(0)?.takeUnlessEmpty(),
                hidden = args.getBoolean(1) != false,
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "set_shortcut_hidden"
        private const val FUNCTION_NAME = "setShortcutHidden"
    }
}
