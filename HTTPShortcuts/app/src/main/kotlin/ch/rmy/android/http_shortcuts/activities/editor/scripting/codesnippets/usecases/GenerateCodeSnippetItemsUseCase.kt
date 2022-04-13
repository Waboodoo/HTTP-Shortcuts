package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.usecases

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.VibrationUtil
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.CodeSnippetItem
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.CodeSnippetPickerViewModel
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.SectionItem
import ch.rmy.android.http_shortcuts.plugin.TaskerUtil

class GenerateCodeSnippetItemsUseCase(context: Context) {

    private val vibrationUtil = VibrationUtil(context)
    private val taskerUtil = TaskerUtil(context)

    operator fun invoke(initData: CodeSnippetPickerViewModel.InitData, callback: (Event) -> Unit): List<SectionItem> =
        createSectionList(callback) {
            if (initData.includeResponseOptions) {
                section(R.string.dialog_code_snippet_handle_response, R.drawable.ic_handle_response) {
                    item(R.string.dialog_code_snippet_response_body) {
                        insertText("response.body", "")
                    }
                    item(R.string.dialog_code_snippet_response_body_json) {
                        insertText("JSON.parse(response.body)", "")
                    }
                    item(R.string.dialog_code_snippet_response_headers) {
                        insertText("response.headers", "")
                    }
                    item(R.string.dialog_code_snippet_response_header) {
                        insertText("response.getHeader(\"", "\")")
                    }
                    item(R.string.dialog_code_snippet_response_status_code) {
                        insertText("response.statusCode", "")
                    }
                    item(R.string.dialog_code_snippet_response_cookies) {
                        insertText("response.cookies", "")
                    }
                    item(R.string.dialog_code_snippet_response_cookie) {
                        insertText("response.getCookie(\"", "\")")
                    }
                    if (initData.includeNetworkErrorOption) {
                        item(R.string.dialog_code_snippet_response_network_error) {
                            insertText("networkError", "")
                        }
                    }
                }
            }
            section(R.string.dialog_code_snippet_variables, R.drawable.ic_variables) {
                item(R.string.dialog_code_snippet_get_variable) {
                    sendEvent(Event.PickVariableForReading)
                }
                item(R.string.dialog_code_snippet_set_variable) {
                    sendEvent(Event.PickVariableForWriting)
                }
            }
            section(R.string.dialog_code_snippet_shortcut_info, R.drawable.ic_shortcut_info) {
                item(R.string.dialog_code_snippet_get_shortcut_id) {
                    insertText("shortcut.id", "")
                }
                item(R.string.dialog_code_snippet_get_shortcut_name) {
                    insertText("shortcut.name", "")
                }
                item(R.string.dialog_code_snippet_get_shortcut_description) {
                    insertText("shortcut.description", "")
                }
            }
            if (initData.includeFileOptions) {
                section(R.string.dialog_code_snippet_files, R.drawable.ic_files) {
                    item(R.string.dialog_code_snippet_get_file_name) {
                        insertText("selectedFiles[0].name", "")
                    }
                    item(R.string.dialog_code_snippet_get_file_type) {
                        insertText("selectedFiles[0].size", "")
                    }
                    item(R.string.dialog_code_snippet_get_file_size) {
                        insertText("selectedFiles[0].type", "")
                    }
                }
            }
            section(R.string.dialog_code_snippet_user_interaction, R.drawable.ic_user_interaction) {
                item(R.string.action_type_toast_title) {
                    insertText("showToast(\"", "\");\n")
                }
                item(R.string.action_type_dialog_title) {
                    insertText("showDialog(\"Message\"", ", \"Title\");\n")
                }
                item(R.string.action_type_selection_title) {
                    insertText("showSelection({\n\"option1\": \"Option 1\",\n\"option2\": \"Option 2\",\n});\n", "")
                }
                item(R.string.action_type_prompt_title) {
                    insertText("prompt(\"Message", "\");\n")
                }
                item(R.string.action_type_confirm_title) {
                    insertText("confirm(\"Message", "\");\n")
                }
                item(R.string.action_play_sound) {
                    sendEvent(Event.PickNotificationSound)
                }
                item(R.string.action_tts) {
                    insertText("speak(\"", "\");\n")
                }
                    .mapIf(vibrationUtil.canVibrate()) {
                        item(R.string.action_type_vibrate_title) {
                            insertText("vibrate();\n", "")
                        }
                    }
            }
            section(R.string.dialog_code_snippet_modify_shortcuts, R.drawable.ic_modify_shortcuts) {
                item(R.string.action_type_rename_shortcut_title) {
                    pickShortcut(R.string.action_type_rename_shortcut_title) { shortcutPlaceholder ->
                        insertText("renameShortcut($shortcutPlaceholder, \"new name", "\");\n")
                    }
                }
                item(R.string.action_type_change_icon_description) {
                    pickShortcut(R.string.action_type_change_icon_description) { shortcutPlaceholder ->
                        insertText("changeDescription($shortcutPlaceholder, \"new description", "\");\n")
                    }
                }
                item(R.string.action_type_change_icon_title) {
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
                item(R.string.action_type_wait) {
                    insertText("wait(1000 /* milliseconds */", ");\n")
                }
                item(R.string.action_type_abort_execution) {
                    insertText("abort();\n", "")
                }
            }
            section(R.string.dialog_code_snippet_text_processing, R.drawable.ic_text_processing) {
                item("MD5".toLocalizable()) {
                    insertText("hash(\"MD5\", \"", "\");\n")
                }
                item("SHA-1".toLocalizable()) {
                    insertText("hash(\"SHA-1\", \"", "\");\n")
                }
                item("SHA-256".toLocalizable()) {
                    insertText("hash(\"SHA-256\", \"", "\");\n")
                }
                item("SHA-512".toLocalizable()) {
                    insertText("hash(\"SHA-512\", \"", "\");\n")
                }
                item("HMAC MD5".toLocalizable()) {
                    insertText("hmac(\"MD5\", \"key", "\", \"message\");\n")
                }
                item("HMAC SHA-1".toLocalizable()) {
                    insertText("hmac(\"SHA-1\", \"key", "\", \"message\");\n")
                }
                item("HMAC SHA-256".toLocalizable()) {
                    insertText("hmac(\"SHA-256\", \"key", "\", \"message\");\n")
                }
                item("HMAC SHA-512".toLocalizable()) {
                    insertText("hmac(\"SHA-512\", \"key", "\", \"message\");\n")
                }
                item("Base64 Encode".toLocalizable(), R.string.action_type_base64encode_description) {
                    insertText("base64encode(\"", "\");\n")
                }
                item("Base64 Decode".toLocalizable(), R.string.action_type_base64decode_description) {
                    insertText("base64decode(\"", "\");\n")
                }
                item(R.string.action_type_to_string, R.string.action_type_to_string_description) {
                    insertText("toString(", ");\n")
                }
                item(R.string.action_type_to_hex_string, R.string.action_type_to_hex_string_description) {
                    insertText("toHexString(", ");\n")
                }
                item(R.string.action_type_trim_string, R.string.action_type_trim_string_description) {
                    insertText("", ".trim()")
                }
            }
            section(R.string.dialog_code_snippet_misc, R.drawable.ic_misc) {
                item(R.string.action_type_trigger_shortcut_title, R.string.action_type_trigger_shortcut_description) {
                    pickShortcut(R.string.action_type_trigger_shortcut_title) { shortcutPlaceholder ->
                        insertText("enqueueShortcut($shortcutPlaceholder);\n", "")
                    }
                }
                item(R.string.action_copy_to_clipboard_title) {
                    insertText("copyToClipboard(\"", "\");\n")
                }
                item(R.string.action_type_get_wifi_ip_address) {
                    insertText("getWifiIPAddress();\n", "")
                }
                item(R.string.action_type_get_wifi_ssid) {
                    insertText("getWifiSSID();\n", "")
                }
                item(R.string.action_type_open_url_title) {
                    insertText("openUrl(\"https://", "\");\n")
                }
                item(R.string.action_type_open_app_title) {
                    insertText("openApp(\"com.example.package_name", "\");\n")
                }
                item(R.string.action_type_send_intent_title) {
                    insertText("sendIntent({", "});\n")
                }
                if (taskerUtil.isTaskerAvailable()) {
                    item(R.string.action_type_trigger_tasker_title) {
                        sendEvent(Event.PickTaskerTask)
                    }
                }
                item(R.string.action_type_wake_on_lan) {
                    insertText("wakeOnLan(\"", "\");\n")
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
                action: ActionContext.() -> Unit,
            ) {
                item(StringResLocalizable(title), description, action)
            }

            fun item(
                title: Localizable,
                @StringRes description: Int? = null,
                action: ActionContext.() -> Unit,
            ) {
                items.add(
                    CodeSnippetItem(title, description?.let(::StringResLocalizable)) {
                        actionContext.action()
                    }
                )
            }

            fun build() = items
        }

        private class ActionContext(val sendEvent: (Event) -> Unit) {
            fun insertText(textBeforeCursor: String, textAfterCursor: String) {
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
