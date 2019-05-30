package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class ActionFactory(private val context: Context) {

    private val types by lazy {
        listOf(
            DialogActionType(context),
            ExtractBodyActionType(context),
            ExtractStatusCodeActionType(context),
            ExtractHeaderActionType(context),
            ExtractCookieActionType(context),
            SetVariableActionType(context),
            RenameShortcutActionType(context),
            TriggerShortcutActionType(context),
            ToastActionType(context),
            VibrateActionType(context)
        )
    }

    fun fromDTO(actionDTO: ActionDTO): BaseAction = getType(actionDTO.type).fromDTO(actionDTO)

    private fun getType(actionType: String): BaseActionType =
        types.firstOrNull { it.type == actionType }
            ?: UnknownActionType(context)

    val availableActionTypes: List<BaseActionType>
        get() = types.filter { it.isAvailable }

}