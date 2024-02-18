package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class OpenURLActionType
@Inject
constructor(
    private val openURLAction: OpenURLAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = openURLAction,
            params = OpenURLAction.Params(
                url = actionDTO.getString(0) ?: "",
                targetBrowser = TargetBrowser.parse(actionDTO.getString(1) ?: ""),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("openURL"),
        parameters = 2,
    )

    companion object {
        private const val TYPE = "open_url"
        private const val FUNCTION_NAME = "openUrl"
    }
}
