package ch.rmy.android.http_shortcuts.scripting.actions

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.types.Base64DecodeActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.Base64EncodeActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.BaseAction
import ch.rmy.android.http_shortcuts.scripting.actions.types.BaseActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ChangeDescriptionActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ChangeIconActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ConfirmActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.CopyToClipboardActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.DialogActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.EnqueueShortcutActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.GetLocationActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.GetVariableActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.HashActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.HmacActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.OpenAppActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.OpenURLActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ParseXMLActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.PlaySoundActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.RenameShortcutActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ScanBarcodeActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SelectionActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SendIntentActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SetVariableActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.TextToSpeechActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ToHexStringActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ToStringActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ToastActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.TriggerTaskerTaskActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.UUIDActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.VibrateActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.WaitActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.WakeOnLanActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.WifiIPActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.WifiSSIDActionType

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
                ChangeDescriptionActionType(),
                ChangeIconActionType(),
                ConfirmActionType(),
                CopyToClipboardActionType(),
                DialogActionType(),
                EnqueueShortcutActionType(),
                GetLocationActionType(),
                GetVariableActionType(),
                HashActionType(),
                HmacActionType(),
                OpenAppActionType(),
                OpenURLActionType(),
                ParseXMLActionType(),
                PlaySoundActionType(),
                PromptActionType(),
                RenameShortcutActionType(),
                ScanBarcodeActionType(),
                SelectionActionType(),
                SendIntentActionType(),
                SetVariableActionType(),
                TextToSpeechActionType(),
                ToastActionType(),
                ToHexStringActionType(),
                ToStringActionType(),
                TriggerTaskerTaskActionType(),
                UUIDActionType(),
                VibrateActionType(),
                WaitActionType(),
                WakeOnLanActionType(),
                WifiIPActionType(),
                WifiSSIDActionType(),
            )
        }
    }
}
