package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.ActionAlias

class ActionFactory(private val context: Context) {

    private val types by lazy {
        listOf(
            CopyToClipboardActionType(context),
            DialogActionType(context),
            RenameShortcutActionType(context),
            TriggerShortcutActionType(context),
            ToastActionType(context),
            VibrateActionType(context),
            ExtractBodyActionType(context),
            ExtractStatusCodeActionType(context),
            ExtractHeaderActionType(context),
            ExtractCookieActionType(context),
            SetVariableActionType(context)
        )
    }

    fun fromDTO(actionDTO: ActionDTO): BaseAction = getType(actionDTO.type).fromDTO(actionDTO)

    private fun getType(actionType: String): BaseActionType =
        types.firstOrNull { it.type == actionType }
            ?: UnknownActionType(context)

    fun getAliases(): Map<String, ActionAlias> =
        types
            .map { it.type to it.getAlias() }
            .filter { it.second != null }
            .associate { it.first to it.second!! }

}