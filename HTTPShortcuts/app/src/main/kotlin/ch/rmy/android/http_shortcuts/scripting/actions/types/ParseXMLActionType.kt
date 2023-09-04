package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class ParseXMLActionType
@Inject
constructor(
    private val parseXMLAction: ParseXMLAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = parseXMLAction,
            params = ParseXMLAction.Params(
                xmlInput = actionDTO.getString(0) ?: "",
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("parseXml"),
        parameters = 1,
    )

    companion object {
        private const val TYPE = "parseXML"
        private const val FUNCTION_NAME = "parseXML"
    }
}
