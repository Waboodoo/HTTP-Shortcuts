package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class ParseHTMLActionType
@Inject
constructor(
    private val parseHTMLAction: ParseHTMLAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = parseHTMLAction,
            params = ParseHTMLAction.Params(
                htmlInput = actionDTO.getString(0) ?: "",
                query = actionDTO.getString(1) ?: ":root",
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("parseHtml"),
        parameters = 2,
    )

    companion object {
        private const val TYPE = "parseHTML"
        private const val FUNCTION_NAME = "parseHTML"
    }
}
