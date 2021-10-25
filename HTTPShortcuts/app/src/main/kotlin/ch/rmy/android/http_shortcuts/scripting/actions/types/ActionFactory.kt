package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class ActionFactory {

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

    companion object {
        private val types: List<BaseActionType> by lazy {
            listOf(
                Base64DecodeActionType(),
                Base64EncodeActionType(),
                ChangeIconActionType(),
                ConfirmActionType(),
                CopyToClipboardActionType(),
                DialogActionType(),
                GetVariableActionType(),
                HashActionType(),
                HmacActionType(),
                PlaySoundActionType(),
                PromptActionType(),
                RenameShortcutActionType(),
                SelectionActionType(),
                SendIntentActionType(),
                SetVariableActionType(),
                TextToSpeechActionType(),
                ToastActionType(),
                ToHexStringActionType(),
                ToStringActionType(),
                TriggerShortcutActionType(),
                TriggerTaskerTaskActionType(),
                VibrateActionType(),
                WaitActionType(),
                WakeOnLanActionType(),
                WifiIPActionType(),
            )
        }
    }

}