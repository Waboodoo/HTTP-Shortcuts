package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class ActionFactory(private val context: Context) {

    private val types by lazy {
        listOf(
            CopyToClipboardActionType(context),
            DialogActionType(context),
            RenameShortcutActionType(context),
            ChangeIconActionType(context),
            TriggerShortcutActionType(context),
            ToastActionType(context),
            VibrateActionType(context),
            TextToSpeechActionType(context)
        )
    }

    fun fromDTO(actionDTO: ActionDTO): BaseAction? =
        getType(actionDTO.type)
            ?.fromDTO(actionDTO)

    private fun getType(actionType: String): BaseActionType? =
        types.firstOrNull { it.type == actionType }

    fun getAliases(): Map<String, ActionAlias> =
        types
            .map { it.type to it.getAlias() }
            .filter { it.second != null }
            .associate { it.first to it.second!! }

}