package ch.rmy.android.http_shortcuts.scripting.actions

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.types.ActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.Base64DecodeActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.Base64EncodeActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ChangeDescriptionActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ChangeIconActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ConfirmActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.CopyToClipboardActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.DialogActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.EnqueueShortcutActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ExecuteShortcutActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.GetClipboardContentActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.GetDirectoryActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.GetLocationActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.GetVariableActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.HashActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.HmacActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.HtmlDecodeActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.HtmlEncodeActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.LogEventActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.NotificationActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.OpenAppActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.OpenURLActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ParseHTMLActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ParseXMLActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.PlaySoundActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptColorActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptDateActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptNumberActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptPasswordActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.PromptTimeActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.RenameShortcutActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ScanBarcodeActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SelectionActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SendHttpRequestActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SendIntentActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SendMQTTMessagesActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SendTCPPacketActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SendUDPPacketActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SetCategoryHiddenActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SetResultActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SetShortcutHiddenActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SetVariableActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.SetWireguardTunnelStateActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.ShareTextActionType
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
import ch.rmy.android.http_shortcuts.scripting.actions.types.WindowActionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionFactory
@Inject
constructor(
    base64DecodeActionType: Base64DecodeActionType,
    base64EncodeActionType: Base64EncodeActionType,
    changeDescriptionActionType: ChangeDescriptionActionType,
    changeIconActionType: ChangeIconActionType,
    confirmActionType: ConfirmActionType,
    copyToClipboardActionType: CopyToClipboardActionType,
    dialogActionType: DialogActionType,
    enqueueShortcutActionType: EnqueueShortcutActionType,
    executeShortcutActionType: ExecuteShortcutActionType,
    getClipboardContentActionType: GetClipboardContentActionType,
    getDirectoryActionType: GetDirectoryActionType,
    getLocationActionType: GetLocationActionType,
    getVariableActionType: GetVariableActionType,
    hashActionType: HashActionType,
    hmacActionType: HmacActionType,
    htmlDecodeActionType: HtmlDecodeActionType,
    htmlEncodeActionType: HtmlEncodeActionType,
    logEventActionType: LogEventActionType,
    notificationActionType: NotificationActionType,
    openAppActionType: OpenAppActionType,
    openURLActionType: OpenURLActionType,
    parseHTMLActionType: ParseHTMLActionType,
    parseXMLActionType: ParseXMLActionType,
    playSoundActionType: PlaySoundActionType,
    promptActionType: PromptActionType,
    promptColorActionType: PromptColorActionType,
    promptDateActionType: PromptDateActionType,
    promptNumberActionType: PromptNumberActionType,
    promptPasswordActionType: PromptPasswordActionType,
    promptTimeActionType: PromptTimeActionType,
    renameShortcutActionType: RenameShortcutActionType,
    scanBarcodeActionType: ScanBarcodeActionType,
    selectionActionType: SelectionActionType,
    sendHttpRequestActionType: SendHttpRequestActionType,
    sendIntentActionType: SendIntentActionType,
    sendMQTTMessagesActionType: SendMQTTMessagesActionType,
    sendTCPPacketActionType: SendTCPPacketActionType,
    sendUDPPacketActionType: SendUDPPacketActionType,
    setCategoryHiddenActionType: SetCategoryHiddenActionType,
    setShortcutHiddenActionType: SetShortcutHiddenActionType,
    setResultActionType: SetResultActionType,
    setVariableActionType: SetVariableActionType,
    setWireguardTunnelStateActionType: SetWireguardTunnelStateActionType,
    shareTextActionType: ShareTextActionType,
    textToSpeechActionType: TextToSpeechActionType,
    toastActionType: ToastActionType,
    toHexStringActionType: ToHexStringActionType,
    toStringActionType: ToStringActionType,
    triggerTaskerTaskActionType: TriggerTaskerTaskActionType,
    uuidActionType: UUIDActionType,
    vibrateActionType: VibrateActionType,
    waitActionType: WaitActionType,
    wakeOnLanActionType: WakeOnLanActionType,
    wifiIPActionType: WifiIPActionType,
    wifiSSIDActionType: WifiSSIDActionType,
    windowActionType: WindowActionType,
) {
    fun getType(actionType: String): ActionType? =
        types[actionType]

    fun getAliases(): Map<String, ActionAlias> =
        types
            .mapValues { (_, type) -> type.getAlias() }
            .filter { it.value != null }
            .mapValues { it.value!! }

    private val types: Map<String, ActionType> =
        listOf(
            base64DecodeActionType,
            base64EncodeActionType,
            changeDescriptionActionType,
            changeIconActionType,
            confirmActionType,
            copyToClipboardActionType,
            getDirectoryActionType,
            dialogActionType,
            enqueueShortcutActionType,
            executeShortcutActionType,
            getClipboardContentActionType,
            getLocationActionType,
            getVariableActionType,
            hashActionType,
            hmacActionType,
            htmlDecodeActionType,
            htmlEncodeActionType,
            logEventActionType,
            notificationActionType,
            openAppActionType,
            openURLActionType,
            parseHTMLActionType,
            parseXMLActionType,
            playSoundActionType,
            promptActionType,
            promptColorActionType,
            promptDateActionType,
            promptNumberActionType,
            promptPasswordActionType,
            promptTimeActionType,
            renameShortcutActionType,
            scanBarcodeActionType,
            selectionActionType,
            sendHttpRequestActionType,
            sendIntentActionType,
            sendMQTTMessagesActionType,
            sendTCPPacketActionType,
            sendUDPPacketActionType,
            setCategoryHiddenActionType,
            setShortcutHiddenActionType,
            setResultActionType,
            setVariableActionType,
            setWireguardTunnelStateActionType,
            shareTextActionType,
            textToSpeechActionType,
            toastActionType,
            toHexStringActionType,
            toStringActionType,
            triggerTaskerTaskActionType,
            uuidActionType,
            vibrateActionType,
            waitActionType,
            wakeOnLanActionType,
            wifiIPActionType,
            wifiSSIDActionType,
            windowActionType,
        )
            .associateBy(ActionType::type)
}
