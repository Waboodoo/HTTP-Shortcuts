package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.annotation.SuppressLint
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class TermuxActionType
@Inject
constructor(
    private val termuxAction: TermuxAction,
) : ActionType {
    override val type = TYPE

    @SuppressLint("SdCardPath")
    override fun getActionRunnable(args: JsFunctionArgs): ActionRunnable<*> {
        val params = args.getObject(0) ?: emptyMap()

        val actionParams = if (!params.isEmpty()) {
            TermuxAction.Params(
                commandPath = params["command"]?.toString() ?: "",
                arguments = (params["arguments"] as? List<*>)
                    ?.map { it.toString() }
                    ?: emptyList(),
                workingDir = params["workingDir"]?.toString(),
                resultDir = params["resultDir"]?.toString(),
                background = params["background"]?.toString()?.toBoolean() == true,
                sessionAction = params["sessionAction"]?.toString() ?: "0",
            )
        } else {
            TermuxAction.Params(
                commandPath = args.getString(0) ?: "",
                arguments = args.getListOfStrings(1) ?: emptyList(),
            )
        }

        return ActionRunnable(
            action = termuxAction,
            params = actionParams,
        )
    }

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "run_termux_command"
        private const val FUNCTION_NAME = "runTermuxCommand"
    }
}
