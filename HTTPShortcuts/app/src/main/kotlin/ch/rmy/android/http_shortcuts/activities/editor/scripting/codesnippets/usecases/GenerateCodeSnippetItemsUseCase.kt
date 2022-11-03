package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.usecases

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.rmy.android.framework.extensions.runIfNotNull
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
import ch.rmy.android.http_shortcuts.utils.SearchUtil.normalizeToKeywords
import ch.rmy.android.http_shortcuts.utils.VibrationUtil
import javax.inject.Inject

class GenerateCodeSnippetItemsUseCase
@Inject
constructor(
    private val context: Context,
    private val vibrationUtil: VibrationUtil,
    private val cameraUtil: CameraUtil,
    private val taskerUtil: TaskerUtil,
    private val playServicesUtil: PlayServicesUtil,
) {

    operator fun invoke(initData: CodeSnippetPickerViewModel.InitData, callback: (Event) -> Unit): List<SectionItem> =
        createSectionList(context, callback) {
            if (initData.includeResponseOptions) {
                section(R.string.dialog_code_snippet_handle_response, R.drawable.ic_handle_response) {
                    item(
                        R.string.dialog_code_snippet_response_body,
                        docRef = "handle-response",
                        keywords = setOf("response", "body"),
                    ) {
                        insertText("response.body")
                    }
                    item(
                        R.string.dialog_code_snippet_response_body_json,
                        docRef = "handle-response",
                        keywords = setOf("response", "body", "json"),
                    ) {
                        insertText("JSON.parse(response.body)")
                    }
                    item(
                        R.string.dialog_code_snippet_response_headers,
                        docRef = "response-headers",
                        keywords = setOf("response", "headers"),
                    ) {
                        insertText("response.headers")
                    }
                    item(
                        R.string.dialog_code_snippet_response_header,
                        docRef = "response-headers",
                        keywords = setOf("response", "headers"),
                    ) {
                        insertText("response.getHeader(\"", "\")")
                    }
                    item(
                        R.string.dialog_code_snippet_response_status_code,
                        docRef = "response-status",
                        keywords = setOf("response", "status", "code"),
                    ) {
                        insertText("response.statusCode")
                    }
                    item(
                        R.string.dialog_code_snippet_response_cookies,
                        docRef = "response-cookies",
                        keywords = setOf("response", "cookies"),
                    ) {
                        insertText("response.cookies")
                    }
                    item(
                        R.string.dialog_code_snippet_response_cookie,
                        docRef = "response-cookies",
                        keywords = setOf("response", "cookies"),
                    ) {
                        insertText("response.getCookie(\"", "\")")
                    }
                    if (initData.includeNetworkErrorOption) {
                        item(
                            R.string.dialog_code_snippet_response_network_error,
                            docRef = "response-errors",
                            keywords = setOf("response", "error", "network"),
                        ) {
                            insertText("networkError")
                        }
                    }
                }
            }
            section(R.string.dialog_code_snippet_variables, R.drawable.ic_variables) {
                item(
                    R.string.dialog_code_snippet_get_variable,
                    docRef = "variables",
                    keywords = setOf("variable", "get", "read"),
                ) {
                    sendEvent(Event.PickVariableForReading)
                }
                item(
                    R.string.dialog_code_snippet_set_variable,
                    docRef = "variables",
                    keywords = setOf("variable", "write", "store", "update", "set"),
                ) {
                    sendEvent(Event.PickVariableForWriting)
                }
            }
            section(R.string.dialog_code_snippet_shortcut_info, R.drawable.ic_info) {
                item(
                    R.string.dialog_code_snippet_get_shortcut_id,
                    docRef = "shortcut-info",
                    keywords = setOf("shortcut", "id"),
                ) {
                    insertText("shortcut.id")
                }
                item(
                    R.string.dialog_code_snippet_get_shortcut_name,
                    docRef = "shortcut-info",
                    keywords = setOf("shortcut", "name", "title", "label"),
                ) {
                    insertText("shortcut.name")
                }
                item(
                    R.string.dialog_code_snippet_get_shortcut_description,
                    docRef = "shortcut-info",
                    keywords = setOf("shortcut", "description"),
                ) {
                    insertText("shortcut.description")
                }
            }
            if (initData.includeFileOptions) {
                section(R.string.dialog_code_snippet_files, R.drawable.ic_files) {
                    item(
                        R.string.dialog_code_snippet_get_file_name,
                        docRef = "files",
                        keywords = setOf("files", "selected", "name"),
                    ) {
                        insertText("selectedFiles[0].name")
                    }
                    item(
                        R.string.dialog_code_snippet_get_file_type,
                        docRef = "files",
                        keywords = setOf("files", "selected", "types"),
                    ) {
                        insertText("selectedFiles[0].size")
                    }
                    item(
                        R.string.dialog_code_snippet_get_file_size,
                        docRef = "files",
                        keywords = setOf("files", "selected", "size"),
                    ) {
                        insertText("selectedFiles[0].type")
                    }
                }
            }
            section(R.string.dialog_code_snippet_user_interaction, R.drawable.ic_user_interaction) {
                item(
                    R.string.action_type_toast_title,
                    docRef = "show-toast",
                    keywords = setOf("toast", "display", "show", "output", "popup"),
                ) {
                    insertText("showToast(\"", "\");\n")
                }
                item(
                    R.string.action_type_dialog_title,
                    docRef = "show-dialog",
                    keywords = setOf("dialog", "display", "show", "output", "popup", "alert"),
                ) {
                    insertText("showDialog(\"Message\"", ", \"Title\");\n")
                }
                item(
                    R.string.action_type_selection_title,
                    docRef = "show-selection",
                    keywords = setOf("dialog", "display", "show", "options"),
                ) {
                    insertText("showSelection({\n\"option1\": \"Option 1\",\n\"option2\": \"Option 2\",\n});\n")
                }
                item(
                    R.string.action_type_prompt_title,
                    docRef = "prompt-confirm",
                    keywords = setOf("dialog", "display", "show", "prompt", "text", "input"),
                ) {
                    insertText("prompt(\"Message", "\")")
                }
                item(
                    R.string.action_type_confirm_title,
                    docRef = "prompt-confirm",
                    keywords = setOf("dialog", "display", "ask", "confirm", "input"),
                ) {
                    insertText("confirm(\"Message", "\")")
                }
                item(
                    R.string.action_play_sound,
                    docRef = "play-sound",
                    keywords = setOf("sound", "audio", "beep", "notification", "play", "alert", "ringtone"),
                ) {
                    sendEvent(Event.PickNotificationSound)
                }
                item(
                    R.string.action_tts,
                    docRef = "speak",
                    keywords = setOf("text", "read", "say", "audio", "sound"),
                ) {
                    insertText("speak(\"", "\");\n")
                }
                if (vibrationUtil.canVibrate()) {
                    item(
                        R.string.action_type_vibrate_title,
                        docRef = "vibrate",
                        keywords = setOf("vibrator", "haptic"),
                    ) {
                        insertText("vibrate();\n")
                    }
                }
                if (cameraUtil.hasCamera()) {
                    item(
                        R.string.action_type_scan_barcode_title,
                        description = R.string.action_type_scan_barcode_description,
                        docRef = "scan-barcode",
                        keywords = setOf("scanner", "qr", "read", "camera"),
                    ) {
                        insertText("scanBarcode()")
                    }
                }
            }
            section(R.string.dialog_code_snippet_modify_shortcuts, R.drawable.ic_modify_shortcuts) {
                item(
                    R.string.action_type_rename_shortcut_title,
                    docRef = "rename-shortcut",
                    keywords = setOf("name", "change", "update", "label", "text"),
                ) {
                    pickShortcut(R.string.action_type_rename_shortcut_title) { shortcutPlaceholder ->
                        insertText("renameShortcut($shortcutPlaceholder, \"new name", "\");\n")
                    }
                }
                item(
                    R.string.action_type_change_icon_description,
                    docRef = "change-description",
                    keywords = setOf("change", "update", "text"),
                ) {
                    pickShortcut(R.string.action_type_change_icon_description) { shortcutPlaceholder ->
                        insertText("changeDescription($shortcutPlaceholder, \"new description", "\");\n")
                    }
                }
                item(
                    R.string.action_type_change_icon_title,
                    docRef = "change-icon",
                    keywords = setOf("symbol", "change", "update", "button"),
                ) {
                    pickShortcut(R.string.action_type_change_icon_title) { shortcutPlaceholder ->
                        sendEvent(Event.PickIcon(shortcutPlaceholder))
                    }
                }
            }
            section(R.string.dialog_code_snippet_control_flow, R.drawable.ic_control_flow) {
                item(
                    "if { }".toLocalizable(),
                    keywords = setOf("condition", "if", "control", "check", "predicate"),
                ) {
                    insertText("if () {\n    ", "\n}\n")
                }
                item(
                    "if { } else { }".toLocalizable(),
                    keywords = setOf("condition", "if", "control", "check", "predicate"),
                ) {
                    insertText("if () {\n    ", "\n} else {\n    \n}\n")
                }
                item(
                    R.string.action_type_wait,
                    docRef = "wait",
                    keywords = setOf("sleep", "delay", "time"),
                ) {
                    insertText("wait(1000 /* milliseconds */", ");\n")
                }
                item(
                    R.string.action_type_abort_execution,
                    docRef = "abort",
                    keywords = setOf("stop", "cancel", "exit"),
                ) {
                    insertText("abort();\n")
                }
            }
            section(R.string.dialog_code_snippet_text_processing, R.drawable.ic_text_processing) {
                item(
                    R.string.action_type_parse_json,
                    keywords = setOf("parse", "json", "read"),
                ) {
                    insertText("JSON.parse(\"", "\")")
                }
                item(
                    R.string.action_type_parse_xml,
                    docRef = "parse-xml",
                    keywords = setOf("parse", "xml", "read"),
                ) {
                    insertText("parseXML(\"", "\")")
                }
                item(
                    "MD5".toLocalizable(),
                    docRef = "hash",
                    keywords = setOf("md5"),
                ) {
                    insertText("hash(\"MD5\", \"", "\")")
                }
                item(
                    "SHA-1".toLocalizable(),
                    docRef = "hash",
                    keywords = setOf("sha1"),
                ) {
                    insertText("hash(\"SHA-1\", \"", "\")")
                }
                item(
                    "SHA-256".toLocalizable(),
                    docRef = "hash",
                    keywords = setOf("sha256"),
                ) {
                    insertText("hash(\"SHA-256\", \"", "\")")
                }
                item(
                    "SHA-512".toLocalizable(),
                    docRef = "hash",
                    keywords = setOf("sha512"),
                ) {
                    insertText("hash(\"SHA-512\", \"", "\")")
                }
                item(
                    "HMAC MD5".toLocalizable(),
                    docRef = "hmac",
                    keywords = setOf("hmac", "md5"),
                ) {
                    insertText("hmac(\"MD5\", \"key", "\", \"message\")")
                }
                item(
                    "HMAC SHA-1".toLocalizable(),
                    docRef = "hmac",
                    keywords = setOf("hmac", "sha1"),
                ) {
                    insertText("hmac(\"SHA-1\", \"key", "\", \"message\")")
                }
                item(
                    "HMAC SHA-256".toLocalizable(),
                    docRef = "hmac",
                    keywords = setOf("hmac", "sha256"),
                ) {
                    insertText("hmac(\"SHA-256\", \"key", "\", \"message\")")
                }
                item(
                    "HMAC SHA-512".toLocalizable(),
                    docRef = "hmac",
                    keywords = setOf("hmac", "sha512"),
                ) {
                    insertText("hmac(\"SHA-512\", \"key", "\", \"message\")")
                }
                item(
                    "Base64 Encode".toLocalizable(),
                    R.string.action_type_base64encode_description,
                    docRef = "base-64",
                    keywords = setOf("base64", "binary", "bytes", "encode", "text"),
                ) {
                    insertText("base64encode(\"", "\")")
                }
                item(
                    "Base64 Decode".toLocalizable(),
                    R.string.action_type_base64decode_description,
                    docRef = "base-64",
                    keywords = setOf("base64", "binary", "bytes", "decode", "text"),
                ) {
                    insertText("base64decode(\"", "\")")
                }
                item(
                    R.string.action_type_to_string,
                    R.string.action_type_to_string_description,
                    docRef = "to-string-to-hex-string",
                    keywords = setOf("hexadecimal", "encode", "decode"),
                ) {
                    insertText("toString(", ")")
                }
                item(
                    R.string.action_type_to_hex_string,
                    R.string.action_type_to_hex_string_description,
                    docRef = "to-string-to-hex-string",
                    keywords = setOf("hexadecimal", "encode", "decode"),
                ) {
                    insertText("toHexString(", ")")
                }
                item(
                    R.string.action_type_trim_string,
                    R.string.action_type_trim_string_description,
                    keywords = setOf("trim", "remove", "whitespace", "empty"),
                ) {
                    insertText("", ".trim()")
                }
            }
            section(R.string.dialog_code_snippet_network, R.drawable.ic_network) {
                item(
                    R.string.action_type_get_wifi_ip_address,
                    docRef = "get-wifi-ip-address",
                    keywords = setOf("ipv4", "network", "wireless"),
                ) {
                    insertText("getWifiIPAddress()")
                }
                item(
                    R.string.action_type_get_wifi_ssid,
                    docRef = "get-wifi-ssid",
                    keywords = setOf("name", "network", "wireless"),
                ) {
                    insertText("getWifiSSID()")
                }
                item(
                    R.string.action_type_wake_on_lan,
                    docRef = "wol",
                    keywords = setOf("wake", "lan", "wakeonlan", "start", "turn", "on", "power", "packet"),
                ) {
                    insertText("wakeOnLan(\"", "\");\n")
                }
                item(
                    R.string.action_type_send_mqtt_message,
                    docRef = "send-mqtt-message",
                    keywords = setOf("network", "client", "publish"),
                ) {
                    insertText(
                        "sendMQTTMessages(\"tcp://broker:port\", {\"username\": \"\", \"password\": \"\"}, [\n    " +
                            "{\"topic\": \"\", \"payload\": \"\"},\n]};\n",
                        "",
                    )
                }
                item(
                    R.string.action_type_send_tcp_packet,
                    docRef = "send-tcp-packet",
                    keywords = setOf("network"),
                ) {
                    insertText("sendTCPPacket(\"message", "\", \"host\", 1337);\n")
                }
                item(
                    R.string.action_type_send_udp_packet,
                    docRef = "send-udp-packet",
                    keywords = setOf("network"),
                ) {
                    insertText("sendUDPPacket(\"message", "\", \"host\", 1337);\n")
                }
            }
            section(R.string.dialog_code_snippet_misc, R.drawable.ic_misc) {
                item(
                    R.string.action_type_generate_uuid,
                    docRef = "uuid-v4",
                    keywords = setOf("random", "id", "guid"),
                ) {
                    insertText("uuidv4()")
                }
                item(
                    R.string.action_type_execute_shortcut_title,
                    R.string.action_type_execute_shortcut_description,
                    docRef = "execute-shortcut",
                    keywords = setOf("trigger", "start", "invoke", "execute", "immediately", "preemptively", "call", "nested", "recursive"),
                ) {
                    pickShortcut(R.string.action_type_execute_shortcut_title) { shortcutPlaceholder ->
                        insertText("executeShortcut($shortcutPlaceholder);\n")
                    }
                }
                item(
                    R.string.action_type_trigger_shortcut_title,
                    R.string.action_type_trigger_shortcut_description,
                    docRef = "trigger-shortcut",
                    keywords = setOf("enqueue", "start", "invoke", "execute"),
                ) {
                    pickShortcut(R.string.action_type_trigger_shortcut_title) { shortcutPlaceholder ->
                        insertText("enqueueShortcut($shortcutPlaceholder);\n")
                    }
                }
                item(
                    R.string.action_get_clipboard_content_title,
                    docRef = "get-clipboard-content",
                    keywords = setOf("copy", "paste", "read"),
                ) {
                    insertText("getClipboardContent()")
                }
                item(
                    R.string.action_copy_to_clipboard_title,
                    docRef = "copy-to-clipboard",
                    keywords = setOf("copy", "paste", "read"),
                ) {
                    insertText("copyToClipboard(\"", "\");\n")
                }
                item(
                    R.string.action_type_open_url_title,
                    docRef = "open-url",
                    keywords = setOf("browser", "send", "start"),
                ) {
                    insertText("openUrl(\"https://", "\");\n")
                }
                item(
                    R.string.action_type_open_app_title,
                    docRef = "open-app",
                    keywords = setOf("open", "start", "application"),
                ) {
                    insertText("openApp(\"com.example.package_name", "\");\n")
                }
                item(
                    R.string.action_type_send_intent_title,
                    docRef = "send-intent",
                    keywords = setOf("android", "app", "invoke", "open", "send"),
                ) {
                    insertText("sendIntent({", "});\n")
                }
                if (taskerUtil.isTaskerAvailable()) {
                    item(
                        R.string.action_type_trigger_tasker_title,
                        docRef = "trigger-tasker-task",
                        keywords = setOf("start", "invoke", "execute"),
                    ) {
                        sendEvent(Event.PickTaskerTask)
                    }
                }
                if (playServicesUtil.isPlayServicesAvailable()) {
                    item(
                        R.string.action_type_get_location_title,
                        description = R.string.action_type_get_location_description,
                        docRef = "get-location",
                        keywords = setOf("locate", "gps", "coordinates"),
                    ) {
                        insertText("getLocation()")
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
        private fun createSectionList(context: Context, callback: (Event) -> Unit, buildList: SectionListBuilder.() -> Unit): List<SectionItem> =
            SectionListBuilder(context, ActionContext(callback))
                .apply(buildList)
                .build()

        private class SectionListBuilder(private val context: Context, private val actionContext: ActionContext) {

            private val items = mutableListOf<SectionItem>()

            fun section(
                @StringRes title: Int,
                @DrawableRes icon: Int,
                buildItems: ItemListBuilder.() -> Unit,
            ) {
                items.add(SectionItem(StringResLocalizable(title), icon, ItemListBuilder(context, actionContext).apply(buildItems).build()))
            }

            fun build() = items
        }

        private class ItemListBuilder(private val context: Context, private val actionContext: ActionContext) {

            private val items = mutableListOf<CodeSnippetItem>()

            fun item(
                @StringRes title: Int,
                @StringRes description: Int? = null,
                docRef: String? = null,
                keywords: Set<String> = emptySet(),
                action: ActionContext.() -> Unit,
            ) {
                item(StringResLocalizable(title), description, docRef, keywords, action)
            }

            fun item(
                title: Localizable,
                @StringRes description: Int? = null,
                docRef: String? = null,
                keywords: Set<String> = emptySet(),
                action: ActionContext.() -> Unit,
            ) {
                val combinedKeywords = keywords
                    .plus(normalizeToKeywords(title.localize(context).toString()))
                    .runIfNotNull(description) {
                        plus(normalizeToKeywords(context.getString(it)))
                    }
                    .runIfNotNull(docRef) {
                        plus(normalizeToKeywords(it))
                    }
                items.add(
                    CodeSnippetItem(title, description?.let(::StringResLocalizable), docRef, combinedKeywords) {
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
