package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class ShareTextActionType
@Inject
constructor(
    private val shareTextAction: ShareTextAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = shareTextAction,
            params = ShareTextAction.Params(
                text = actionDTO.getString(0) ?: "",
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 1,
    )

    companion object {
        private const val TYPE = "share_text"
        private const val FUNCTION_NAME = "shareText"
    }
}
