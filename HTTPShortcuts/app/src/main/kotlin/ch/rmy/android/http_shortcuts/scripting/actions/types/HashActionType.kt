package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class HashActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = HashAction(
        algorithm = actionDTO[KEY_ALGORITHM]?.toLowerCase() ?: "",
        text = actionDTO[KEY_TEXT] ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_ALGORITHM, KEY_TEXT),
    )

    companion object {

        const val TYPE = "hash"
        const val FUNCTION_NAME = "hash"

        const val KEY_ALGORITHM = "algorithm"
        const val KEY_TEXT = "text"

    }

}