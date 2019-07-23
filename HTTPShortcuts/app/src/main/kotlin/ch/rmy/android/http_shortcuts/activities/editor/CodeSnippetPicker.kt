package ch.rmy.android.http_shortcuts.activities.editor

import android.content.Context
import android.os.Vibrator
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.extensions.insertAroundCursor
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider

class CodeSnippetPicker(private val context: Context, private val variablePlaceholderProvider: VariablePlaceholderProvider) {

    fun showCodeSnippetPicker(target: EditText, includeResponseOptions: Boolean = true, includeNetworkErrorOption: Boolean = false) {
        MenuDialogBuilder(context)
            .title(R.string.title_add_code_snippet)
            .mapIf(includeResponseOptions) {
                it.item(R.string.dialog_code_snippet_handle_response) {
                    showResponseOptionsPicker(target, includeNetworkErrorOption)
                }
            }
            .item(R.string.dialog_code_snippet_variables) {
                showVariablesOptionsPicker(target)
            }
            .item(R.string.dialog_code_snippet_actions) {
                showActionsPicker(target)
            }
            .showIfPossible()
    }

    private fun showResponseOptionsPicker(target: EditText, includeNetworkErrorOption: Boolean = false) {
        MenuDialogBuilder(context)
            .item(R.string.dialog_code_snippet_response_body) {
                target.insertAroundCursor("response.body")
            }
            .item(R.string.dialog_code_snippet_response_headers) {
                target.insertAroundCursor("response.headers")
            }
            .item(R.string.dialog_code_snippet_response_status_code) {
                target.insertAroundCursor("response.statusCode")
            }
            .item(R.string.dialog_code_snippet_response_cookies) {
                target.insertAroundCursor("response.cookies")
            }
            .mapIf(includeNetworkErrorOption) {
                it.item(R.string.dialog_code_snippet_response_network_error) {
                    target.insertAroundCursor("networkError")
                }
            }
            .showIfPossible()
    }

    private fun showVariablesOptionsPicker(target: EditText) {
        MenuDialogBuilder(context)
            .item(R.string.dialog_code_snippet_get_variable) {
                MenuDialogBuilder(context)
                    .mapFor(variablePlaceholderProvider.placeholders) { builder, variable ->
                        builder.item(variable.variableKey) {
                            target.insertAroundCursor("getVariable(\"${variable.variableKey}\")")
                        }
                    }
                    .showIfPossible()
            }
            .item(R.string.dialog_code_snippet_set_variable) {
                MenuDialogBuilder(context)
                    .mapFor(variablePlaceholderProvider.constantsPlaceholders) { builder, variable ->
                        builder.item(variable.variableKey) {
                            target.insertAroundCursor("setVariable(\"${variable.variableKey}\", \"", "\");")
                        }
                    }
                    .showIfPossible()
            }
            .showIfPossible()
    }

    private fun showActionsPicker(target: EditText) {
        MenuDialogBuilder(context)
            .item(R.string.action_type_toast_title) {
                target.insertAroundCursor("showToast(\"", "\");")
            }
            .item(R.string.action_type_dialog_title) {
                target.insertAroundCursor("showDialog(\"Message\", \"\", \"Title\");")
            }
            .mapIf((context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).hasVibrator()) {
                it.item(R.string.action_type_vibrate_title) {
                    target.insertAroundCursor("vibrate();")
                }
            }
            .item(R.string.action_type_trigger_shortcut_title) {
                target.insertAroundCursor("triggerShortcut(\"shortcut name or ID\", \"\");")
            }
            .item(R.string.action_type_rename_shortcut_title) {
                target.insertAroundCursor("renameShortcut(\"shortcut name or ID\", \"\", \"new name\");")
            }
            .showIfPossible()
    }

}