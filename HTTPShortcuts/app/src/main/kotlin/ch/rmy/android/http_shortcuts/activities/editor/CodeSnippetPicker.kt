package ch.rmy.android.http_shortcuts.activities.editor

import android.content.Context
import android.os.Vibrator
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.openURL
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider

class CodeSnippetPicker(private val context: Context, private val variablePlaceholderProvider: VariablePlaceholderProvider) {

    fun showCodeSnippetPicker(insertText: (before: String, after: String) -> Unit, includeResponseOptions: Boolean = true, includeNetworkErrorOption: Boolean = false) {
        DialogBuilder(context)
            .title(R.string.title_add_code_snippet)
            .mapIf(includeResponseOptions) {
                it.item(R.string.dialog_code_snippet_handle_response) {
                    showResponseOptionsPicker(insertText, includeNetworkErrorOption)
                }
            }
            .item(R.string.dialog_code_snippet_variables) {
                showVariablesOptionsPicker(insertText)
            }
            .item(R.string.dialog_code_snippet_actions) {
                showActionsPicker(insertText)
            }
            .item(R.string.dialog_code_snippet_learn_more) {
                context.openURL(CODE_HELP_URL)
            }
            .showIfPossible()
    }

    private fun showResponseOptionsPicker(insertText: (before: String, after: String) -> Unit, includeNetworkErrorOption: Boolean = false) {
        DialogBuilder(context)
            .item(R.string.dialog_code_snippet_response_body) {
                insertText("response.body", "")
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

    private fun showVariablesOptionsPicker(insertText: (before: String, after: String) -> Unit) {
        if (variablePlaceholderProvider.hasVariables) {
            DialogBuilder(context)
                .item(R.string.dialog_code_snippet_get_variable) {
                    DialogBuilder(context)
                        .mapFor(variablePlaceholderProvider.placeholders) { builder, variable ->
                            builder.item(variable.variableKey) {
                                insertText("getVariable(/*[variable]*/\"${variable.variableId}\"/*[/variable]*/)", "")
                            }
                        }
                        .showIfPossible()
                }
                .item(R.string.dialog_code_snippet_set_variable) {
                    if (variablePlaceholderProvider.hasConstants) {
                        DialogBuilder(context)
                            .mapFor(variablePlaceholderProvider.constantsPlaceholders) { builder, variable ->
                                builder.item(variable.variableKey) {
                                    insertText("setVariable(/*[variable]*/\"${variable.variableId}\"/*[/variable]*/, \"", "\");")
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

    private fun showActionsPicker(insertText: (before: String, after: String) -> Unit) {
        // TODO: Avoid duplicate code and decouple code snippet picker from action declarion
        // -> move action declaration to actions themselves
        DialogBuilder(context)
            .item(R.string.action_type_toast_title) {
                insertText("showToast(\"", "\");")
            }
            .item(R.string.action_type_dialog_title) {
                insertText("showDialog(\"Message\"", ", \"Title\");")
            }
            .item(R.string.action_copy_to_clipboard_title) {
                insertText("copyToClipboard(\"", "\");")
            }
            .mapIf((context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()) {
                it.item(R.string.action_type_vibrate_title) {
                    insertText("vibrate();", "")
                }
            }
            .item(R.string.action_type_trigger_shortcut_title) {
                insertText("triggerShortcut(\"shortcut name or ID", "\");")
            }
            .mapIf(LauncherShortcutManager.supportsPinning(context)) {
                it.item(R.string.action_type_rename_shortcut_title) {
                    insertText("renameShortcut(\"shortcut name or ID\", \"new name", "\");")
                }
            }
            .showIfPossible()
    }

    companion object {
        private const val CODE_HELP_URL = "https://http-shortcuts.rmy.ch/#code"
    }

}