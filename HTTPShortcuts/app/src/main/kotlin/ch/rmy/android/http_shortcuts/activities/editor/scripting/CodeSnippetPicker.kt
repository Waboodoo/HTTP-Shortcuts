package ch.rmy.android.http_shortcuts.activities.editor.scripting

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Vibrator
import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.plugin.TaskerIntent
import ch.rmy.android.http_shortcuts.scripting.actions.types.TriggerTaskerTaskActionType
import ch.rmy.android.http_shortcuts.scripting.actions.types.TriggerTaskerTaskActionType.Companion.REQUEST_CODE_SELECT_TASK
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider

class CodeSnippetPicker(
    private val context: Context,
    private val currentShortcutId: String?,
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
    private val shortcutPlaceholderProvider: ShortcutPlaceholderProvider,
    private val openIconPicker: (String) -> Unit,
) {

    fun showCodeSnippetPicker(
        insertText: InsertText,
        includeResponseOptions: Boolean = true,
        includeNetworkErrorOption: Boolean = false,
        includeFileOptions: Boolean = true,
    ) {
        DialogBuilder(context)
            .title(R.string.title_add_code_snippet)
            .mapIf(includeResponseOptions) {
                it.item(R.string.dialog_code_snippet_handle_response, iconRes = R.drawable.ic_handle_response) {
                    showResponseOptionsPicker(insertText, includeNetworkErrorOption)
                }
            }
            .item(R.string.dialog_code_snippet_variables, iconRes = R.drawable.ic_variables) {
                showVariablesOptionsPicker(insertText)
            }
            .item(R.string.dialog_code_snippet_shortcut_info, iconRes = R.drawable.ic_shortcut_info) {
                showShortcutInfoPicker(insertText)
            }
            .mapIf(includeFileOptions) {
                it.item(R.string.dialog_code_snippet_files, iconRes = R.drawable.ic_files) {
                    showFilesPicker(insertText)
                }
            }
            .item(R.string.dialog_code_snippet_user_interaction, iconRes = R.drawable.ic_user_interaction) {
                showUserInteractionPicker(insertText)
            }
            .item(R.string.dialog_code_snippet_modify_shortcuts, iconRes = R.drawable.ic_modify_shortcuts) {
                showModifyShortcutPicker(insertText)
            }
            .item(R.string.dialog_code_snippet_control_flow, iconRes = R.drawable.ic_control_flow) {
                showControlFlowPicker(insertText)
            }
            .item(R.string.dialog_code_snippet_text_processing, iconRes = R.drawable.ic_text_processing) {
                showTextProcessingPicker(insertText)
            }
            .item(R.string.dialog_code_snippet_misc, iconRes = R.drawable.ic_misc) {
                showMiscPicker(insertText)
            }
            .showIfPossible()
    }

    private fun showShortcutInfoPicker(insertText: InsertText) {
        DialogBuilder(context)
            .item(R.string.dialog_code_snippet_get_shortcut_id) {
                insertText("shortcut.id", "")
            }
            .item(R.string.dialog_code_snippet_get_shortcut_name) {
                insertText("shortcut.name", "")
            }
            .item(R.string.dialog_code_snippet_get_shortcut_description) {
                insertText("shortcut.description", "")
            }
            .showIfPossible()
    }

    private fun showFilesPicker(insertText: InsertText) {
        DialogBuilder(context)
            .item(R.string.dialog_code_snippet_get_file_name) {
                insertText("selectedFiles[0].name", "")
            }
            .item(R.string.dialog_code_snippet_get_file_type) {
                insertText("selectedFiles[0].size", "")
            }
            .item(R.string.dialog_code_snippet_get_file_size) {
                insertText("selectedFiles[0].type", "")
            }
            .showIfPossible()
    }

    private fun showResponseOptionsPicker(insertText: InsertText, includeNetworkErrorOption: Boolean = false) {
        DialogBuilder(context)
            .item(R.string.dialog_code_snippet_response_body) {
                insertText("response.body", "")
            }
            .item(R.string.dialog_code_snippet_response_body_json) {
                insertText("JSON.parse(response.body)", "")
            }
            .item(R.string.dialog_code_snippet_response_headers) {
                insertText("response.headers", "")
            }
            .item(R.string.dialog_code_snippet_response_status_code) {
                insertText("response.statusCode", "")
            }
            .item(R.string.dialog_code_snippet_response_cookies) {
                insertText("response.cookies", "")
            }
            .mapIf(includeNetworkErrorOption) {
                it.item(R.string.dialog_code_snippet_response_network_error) {
                    insertText("networkError", "")
                }
            }
            .showIfPossible()
    }

    private fun showUserInteractionPicker(insertText: InsertText) {
        DialogBuilder(context)
            .item(R.string.action_type_toast_title) {
                insertText("showToast(\"", "\");\n")
            }
            .item(R.string.action_type_dialog_title) {
                insertText("showDialog(\"Message\"", ", \"Title\");\n")
            }
            .item(R.string.action_type_selection_title) {
                insertText("showSelection({\n\"option1\": \"Option 1\",\n\"option2\": \"Option 2\",\n});\n", "")
            }
            .item(R.string.action_type_prompt_title) {
                insertText("prompt(\"Message", "\");\n")
            }
            .item(R.string.action_type_confirm_title) {
                insertText("confirm(\"Message", "\");\n")
            }
            .mapIf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.item(R.string.action_tts) {
                    insertText("speak(\"", "\");\n")
                }
            }
            .mapIf((context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()) {
                it.item(R.string.action_type_vibrate_title) {
                    insertText("vibrate();\n", "")
                }
            }
            .showIfPossible()
    }

    private fun showModifyShortcutPicker(insertText: InsertText) {
        DialogBuilder(context)
            .item(R.string.action_type_rename_shortcut_title) {
                actionWithShortcut(R.string.action_type_rename_shortcut_title) { shortcutPlaceholder ->
                    insertText("renameShortcut($shortcutPlaceholder, \"new name", "\");\n")
                }
            }
            .item(R.string.action_type_change_icon_title) {
                actionWithShortcut(R.string.action_type_change_icon_title) { shortcutPlaceholder ->
                    openIconPicker(shortcutPlaceholder)
                }
            }
            .showIfPossible()
    }

    private fun showVariablesOptionsPicker(insertText: InsertText) {
        if (variablePlaceholderProvider.hasVariables) {
            DialogBuilder(context)
                .item(R.string.dialog_code_snippet_get_variable) {
                    DialogBuilder(context)
                        .mapFor(variablePlaceholderProvider.placeholders) { builder, variable ->
                            builder.item(name = variable.variableKey) {
                                insertText("getVariable(/*[variable]*/\"${variable.variableId}\"/*[/variable]*/)", "")
                            }
                        }
                        .showIfPossible()
                }
                .item(R.string.dialog_code_snippet_set_variable) {
                    if (variablePlaceholderProvider.hasConstants) {
                        DialogBuilder(context)
                            .mapFor(variablePlaceholderProvider.constantsPlaceholders) { builder, variable ->
                                builder.item(name = variable.variableKey) {
                                    insertText("setVariable(/*[variable]*/\"${variable.variableId}\"/*[/variable]*/, \"", "\");\n")
                                }
                            }
                            .showIfPossible()
                    } else {
                        openSetVariablesInstructionDialog()
                    }
                }
                .showIfPossible()
        } else {
            openGetVariablesInstructionDialog()
        }
    }

    private fun openGetVariablesInstructionDialog() {
        DialogBuilder(context)
            .title(R.string.help_title_variables)
            .message(R.string.help_text_code_snippet_get_variable_no_variable)
            .negative(android.R.string.cancel)
            .positive(R.string.button_create_first_variable) { openVariableEditor() }
            .show()
    }

    private fun openVariableEditor() {
        VariablesActivity.IntentBuilder(context)
            .build()
            .startActivity(context)
    }

    private fun openSetVariablesInstructionDialog() {
        DialogBuilder(context)
            .title(R.string.help_title_variables)
            .message(R.string.help_text_code_snippet_set_variable_no_variable)
            .negative(android.R.string.cancel)
            .positive(R.string.button_create_first_variable) { openVariableEditor() }
            .show()
    }

    private fun showControlFlowPicker(insertText: InsertText) {
        DialogBuilder(context)
            .item(R.string.action_type_wait) {
                insertText("wait(1000 /* milliseconds */", ");\n")
            }
            .item(R.string.action_type_abort_execution) {
                insertText("abort();\n", "")
            }
            .showIfPossible()
    }

    private fun showTextProcessingPicker(insertText: InsertText) {
        DialogBuilder(context)
            .item(name = "MD5") {
                insertText("hash(\"MD5\", \"", "\");\n")
            }
            .item(name = "SHA-1") {
                insertText("hash(\"SHA-1\", \"", "\");\n")
            }
            .item(name = "SHA-256") {
                insertText("hash(\"SHA-256\", \"", "\");\n")
            }
            .item(name = "SHA-512") {
                insertText("hash(\"SHA-512\", \"", "\");\n")
            }
            .showIfPossible()
    }

    private fun showMiscPicker(insertText: InsertText) {
        DialogBuilder(context)
            .item(R.string.action_type_trigger_shortcut_title) {
                actionWithShortcut(R.string.action_type_trigger_shortcut_title) { shortcutPlaceholder ->
                    insertText("triggerShortcut($shortcutPlaceholder);\n", "")
                }
            }
            .item(R.string.action_copy_to_clipboard_title) {
                insertText("copyToClipboard(\"", "\");\n")
            }
            .item(R.string.action_type_get_wifi_ip_address) {
                insertText("getWifiIPAddress();\n", "")
            }
            .item(R.string.action_type_send_intent_title) {
                insertText("sendIntent({", "});\n")
            }
            .mapIf(TriggerTaskerTaskActionType.isTaskerAvailable(context)) {
                it.item(R.string.action_type_trigger_tasker_title) {
                    try {
                        TaskerIntent.getTaskSelectIntent()
                            .startActivity(context, REQUEST_CODE_SELECT_TASK)
                    } catch (e: ActivityNotFoundException) {
                        logException(e)
                        context.showToast(R.string.error_generic)
                    }
                }
            }
            .showIfPossible()
    }

    private fun actionWithShortcut(@StringRes title: Int, callback: (String) -> Unit) {
        if (shortcutPlaceholderProvider.placeholders.none { it.id != currentShortcutId }) {
            callback("\"\"")
            return
        }
        DialogBuilder(context)
            .title(title)
            .item(R.string.label_insert_action_code_for_current_shortcut) {
                callback("\"\"")
            }
            .mapFor(shortcutPlaceholderProvider.placeholders) { builder, shortcut ->
                builder.mapIf(shortcut.id != currentShortcutId) {
                    it.item(name = shortcut.name, iconName = shortcut.iconName) {
                        callback("/*[shortcut]*/\"${shortcut.id}\"/*[/shortcut]*/")
                    }
                }
            }
            .showIfPossible()
    }

    fun handleRequestResult(insertText: InsertText, requestCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SELECT_TASK -> {
                val taskName = data?.dataString ?: return
                insertText.invoke("${TriggerTaskerTaskActionType.FUNCTION_NAME}(\"${escape(taskName)}\");", "")
            }
        }
    }

    fun insertChangeIconSnippet(shortcutPlaceholder: String, insertText: (String, String) -> Unit, iconName: String) {
        insertText("changeIcon($shortcutPlaceholder, \"$iconName\");\n", "")
    }

    companion object {

        private fun escape(input: String) =
            input.replace("\"", "\\\"")

    }

}