package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.usecases

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.CodeSnippetItem
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.CodeSnippetPickerViewModel
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.SectionItem
import ch.rmy.android.http_shortcuts.plugin.TaskerUtil
import ch.rmy.android.http_shortcuts.utils.CameraUtil
import ch.rmy.android.http_shortcuts.utils.PlayServicesUtil
import ch.rmy.android.http_shortcuts.utils.VibrationUtil
import javax.inject.Inject

class GenerateCodeSnippetItemsUseCase
@Inject
constructor(
    private val vibrationUtil: VibrationUtil,
    private val cameraUtil: CameraUtil,
    private val taskerUtil: TaskerUtil,
    private val playServicesUtil: PlayServicesUtil,
) {

    operator fun invoke(initData: CodeSnippetPickerViewModel.InitData, callback: (Event) -> Unit): List<SectionItem> =
        createSectionList(callback) {
            if (initData.includeResponseOptions) {
                section(R.string.dialog_code_snippet_handle_response, R.drawable.ic_handle_response) {
                    item(R.string.dialog_code_snippet_response_body, docRef = "handle-response") {
                        insertText("response.body")
                    }
                    item(R.string.dialog_code_snippet_response_body_json, docRef = "handle-response") {
                        insertText("JSON.parse(response.body)")
                    }
                    item(R.string.dialog_code_snippet_response_headers, docRef = "response-headers") {
                        insertText("response.headers")
                    }
                    item(R.string.dialog_code_snippet_response_header, docRef = "response-headers") {
                        insertText("response.getHeader(\"", "\")")
                    }
                    item(R.string.dialog_code_snippet_response_status_code, docRef = "response-status") {
                        insertText("response.statusCode")
                    }
                    item(R.string.dialog_code_snippet_response_cookies, docRef = "response-cookies") {
                        insertText("response.cookies")
                    }
                    item(R.string.dialog_code_snippet_response_cookie, docRef = "response-cookies") {
                        insertText("response.getCookie(\"", "\")")
                    }
                    if (initData.includeNetworkErrorOption) {
                        item(R.string.dialog_code_snippet_response_network_error, docRef = "response-errors") {
                            insertText("networkError")
                        }
                    }
                }
            }
            section(R.string.dialog_code_snippet_variables, R.drawable.ic_variables) {
                item(R.string.dialog_code_snippet_get_variable, docRef = "variables") {
                    sendEvent(Event.PickVariableForReading)
                }
                item(R.string.dialog_code_snippet_set_variable, docRef = "variables") {
                    sendEvent(Event.PickVariableForWriting)
                }
            }
            section(R.string.dialog_code_snippet_shortcut_info, R.drawable.ic_info) {
                item(R.string.dialog_code_snippet_get_shortcut_id, docRef = "shortcut-info") {
                    insertText("shortcut.id")
                }
                item(R.string.dialog_code_snippet_get_shortcut_name, docRef = "shortcut-info") {
                    insertText("shortcut.name")
                }
                item(R.string.dialog_code_snippet_get_shortcut_description, docRef = "shortcut-info") {
                    insertText("shortcut.description")
                }
            }
            if (initData.includeFileOptions) {
                section(R.string.dialog_code_snippet_files, R.drawable.ic_files) {
                    item(R.string.dialog_code_snippet_get_file_name, docRef = "files") {
                        insertText("selectedFiles[0].name")
                    }
                    item(R.string.dialog_code_snippet_get_file_type, docRef = "files") {
                        insertText("selectedFiles[0].size")
                    }
                    item(R.string.dialog_code_snippet_get_file_size, docRef = "files") {
                        insertText("selectedFiles[0].type")
                    }
                }
            }
            section(R.string.dialog_code_snippet_user_interaction, R.drawable.ic_user_interaction) {
                item(R.string.action_type_toast_title, docRef = "show-toast") {
                    insertText("showToast(\"", "\");\n")
                }
                item(R.string.action_type_dialog_title, docRef = "show-dialog") {
                    insertText("showDialog(\"Message\"", ", \"Title\");\n")
                }
                item(R.string.action_type_selection_title, docRef = "show-selection") {
                    insertText("showSelection({\n\"option1\": \"Option 1\",\n\"option2\": \"Option 2\",\n});\n")
                }
                item(R.string.action_type_prompt_title, docRef = "prompt-confirm") {
                    insertText("prompt(\"Message", "\");\n")
                }
                item(R.string.action_type_confirm_title, docRef = "prompt-confirm") {
                    insertText("confirm(\"Message", "\");\n")
                }
                item(R.string.action_play_sound, docRef = "play-sound") {
                    sendEvent(Event.PickNotificationSound)
                }
                item(R.string.action_tts, docRef = "speak") {
                    insertText("speak(\"", "\");\n")
                }
                if (vibrationUtil.canVibrate()) {
                    item(R.string.action_type_vibrate_title, docRef = "vibrate") {
                        insertText("vibrate();\n")
                    }
                }
                if (cameraUtil.hasCamera()) {
                    item(
                        R.string.action_type_scan_barcode_title,
                        description = R.string.action_type_scan_barcode_description,
                        docRef = "scan-barcode",
                    ) {
                        insertText("scanBarcode();\n")
                    }
                }
            }
            section(R.string.dialog_code_snippet_modify_shortcuts, R.drawable.ic_modify_shortcuts) {
                item(R.string.action_type_rename_shortcut_title, docRef = "rename-shortcut") {
                    pickShortcut(R.string.action_type_rename_shortcut_title) { shortcutPlaceholder ->
                        insertText("renameShortcut($shortcutPlaceholder, \"new name", "\");\n")
                    }
                }
                item(R.string.action_type_change_icon_description, docRef = "change-description") {
                    pickShortcut(R.string.action_type_change_icon_description) { shortcutPlaceholder ->
                        insertText("changeDescription($shortcutPlaceholder, \"new description", "\");\n")
                    }
                }
                item(R.string.action_type_change_icon_title, docRef = "change-icon") {
                    pickShortcut(R.string.action_type_change_icon_title) { shortcutPlaceholder ->
                        sendEvent(Event.PickIcon(shortcutPlaceholder))
                    }
                }
            }
            section(R.string.dialog_code_snippet_control_flow, R.drawable.ic_control_flow) {
                item("if { }".toLocalizable()) {
                    insertText("if () {\n    ", "\n}\n")
                }
                item("if { } else { }".toLocalizable()) {
                    insertText("if () {\n    ", "\n} else {\n    \n}\n")
                }
                item(R.string.action_type_wait, docRef = "wait") {
                    insertText("wait(1000 /* milliseconds */", ");\n")
                }
                item(R.string.action_type_abort_execution, docRef = "abort") {
                    insertText("abort();\n")
                }
            }
            section(R.string.dialog_code_snippet_text_processing, R.drawable.ic_text_processing) {
                item(R.string.action_type_parse_json) {
                    insertText("JSON.parse(\"", "\");\n")
                }
                item(R.string.action_type_parse_xml, docRef = "parse-xml") {
                    insertText("parseXML(\"", "\");\n")
                }
                item("MD5".toLocalizable(), docRef = "hash") {
                    insertText("hash(\"MD5\", \"", "\");\n")
                }
                item("SHA-1".toLocalizable(), docRef = "hash") {
                    insertText("hash(\"SHA-1\", \"", "\");\n")
                }
                item("SHA-256".toLocalizable(), docRef = "hash") {
                    insertText("hash(\"SHA-256\", \"", "\");\n")
                }
                item("SHA-512".toLocalizable(), docRef = "hash") {
                    insertText("hash(\"SHA-512\", \"", "\");\n")
                }
                item("HMAC MD5".toLocalizable(), docRef = "hmac") {
                    insertText("hmac(\"MD5\", \"key", "\", \"message\");\n")
                }
                item("HMAC SHA-1".toLocalizable(), docRef = "hmac") {
                    insertText("hmac(\"SHA-1\", \"key", "\", \"message\");\n")
                }
                item("HMAC SHA-256".toLocalizable(), docRef = "hmac") {
                    insertText("hmac(\"SHA-256\", \"key", "\", \"message\");\n")
                }
                item("HMAC SHA-512".toLocalizable(), docRef = "hmac") {
                    insertText("hmac(\"SHA-512\", \"key", "\", \"message\");\n")
                }
                item("Base64 Encode".toLocalizable(), R.string.action_type_base64encode_description, docRef = "base-64") {
                    insertText("base64encode(\"", "\");\n")
                }
                item("Base64 Decode".toLocalizable(), R.string.action_type_base64decode_description, docRef = "base-64") {
                    insertText("base64decode(\"", "\");\n")
                }
                item(R.string.action_type_to_string, R.string.action_type_to_string_description, docRef = "to-string-to-hex-string") {
                    insertText("toString(", ");\n")
                }
                item(R.string.action_type_to_hex_string, R.string.action_type_to_hex_string_description, docRef = "to-string-to-hex-string") {
                    insertText("toHexString(", ");\n")
                }
                item(R.string.action_type_trim_string, R.string.action_type_trim_string_description) {
                    insertText("", ".trim()")
                }
            }
            section(R.string.dialog_code_snippet_network, R.drawable.ic_network) {
                item(R.string.action_type_get_wifi_ip_address, docRef = "get-wifi-ip-address") {
                    insertText("getWifiIPAddress();\n")
                }
                item(R.string.action_type_get_wifi_ssid, docRef = "get-wifi-ssid") {
                    insertText("getWifiSSID();\n")
                }
                item(R.string.action_type_wake_on_lan, docRef = "wol") {
                    insertText("wakeOnLan(\"", "\");\n")
                }
            }
            section(R.string.dialog_code_snippet_misc, R.drawable.ic_misc) {
                item(R.string.action_type_generate_uuid, docRef = "uuid-v4") {
                    insertText("uuidv4();")
                }
                item(R.string.action_type_trigger_shortcut_title, R.string.action_type_trigger_shortcut_description, docRef = "trigger-shortcut") {
                    pickShortcut(R.string.action_type_trigger_shortcut_title) { shortcutPlaceholder ->
                        insertText("enqueueShortcut($shortcutPlaceholder);\n")
                    }
                }
                item(R.string.action_copy_to_clipboard_title, docRef = "copy-to-clipboard") {
                    insertText("copyToClipboard(\"", "\");\n")
                }
                item(R.string.action_type_open_url_title, docRef = "open-url") {
                    insertText("openUrl(\"https://", "\");\n")
                }
                item(R.string.action_type_open_app_title, docRef = "open-app") {
                    insertText("openApp(\"com.example.package_name", "\");\n")
                }
                item(R.string.action_type_send_intent_title, docRef = "send-intent") {
                    insertText("sendIntent({", "});\n")
                }
                if (taskerUtil.isTaskerAvailable()) {
                    item(R.string.action_type_trigger_tasker_title, docRef = "trigger-tasker-task") {
                        sendEvent(Event.PickTaskerTask)
                    }
                }
                if (playServicesUtil.isPlayServicesAvailable()) {
                    item(
                        R.string.action_type_get_location_title,
                        description = R.string.action_type_get_location_description,
                        docRef = "get-location",
                    ) {
                        insertText("getLocation();\n")
                    }
                }
            }
        }

    sealed interface Event {
        data class InsertText(val textBeforeCursor: String, val textAfterCursor: String) : Event
        data class PickShortcut(@StringRes val title: Int, val andThen: (shortcutPlaceholder: String) -> Unit) : Event
        data class PickIcon(val shortcutPlaceholder: String) : Event
        object PickVariableForReading : Event
        object PickVariableForWriting : Event
        object PickTaskerTask : Event
        object PickNotificationSound : Event
    }

    companion object {
        private fun createSectionList(callback: (Event) -> Unit, buildList: SectionListBuilder.() -> Unit): List<SectionItem> =
            SectionListBuilder(ActionContext(callback))
                .apply(buildList)
                .build()

        private class SectionListBuilder(private val actionContext: ActionContext) {

            private val items = mutableListOf<SectionItem>()

            fun section(
                @StringRes title: Int,
                @DrawableRes icon: Int,
                buildItems: ItemListBuilder.() -> Unit,
            ) {
                items.add(SectionItem(StringResLocalizable(title), icon, ItemListBuilder(actionContext).apply(buildItems).build()))
            }

            fun build() = items
        }

        private class ItemListBuilder(private val actionContext: ActionContext) {

            private val items = mutableListOf<CodeSnippetItem>()

            fun item(
                @StringRes title: Int,
                @StringRes description: Int? = null,
                docRef: String? = null,
                action: ActionContext.() -> Unit,
            ) {
                item(StringResLocalizable(title), description, docRef, action)
            }

            fun item(
                title: Localizable,
                @StringRes description: Int? = null,
                docRef: String? = null,
                action: ActionContext.() -> Unit,
            ) {
                items.add(
                    CodeSnippetItem(title, description?.let(::StringResLocalizable), docRef) {
                        actionContext.action()
                    }
                )
            }

            fun build() = items
        }

        private class ActionContext(val sendEvent: (Event) -> Unit) {
            fun insertText(textBeforeCursor: String, textAfterCursor: String = "") {
                sendEvent(Event.InsertText(textBeforeCursor, textAfterCursor))
            }

            fun pickShortcut(@StringRes title: Int, action: ActionContext.(shortcutPlaceholder: String) -> Unit) {
                sendEvent(
                    Event.PickShortcut(title) {
                        action(it)
                    }
                )
            }
        }
    }
}
