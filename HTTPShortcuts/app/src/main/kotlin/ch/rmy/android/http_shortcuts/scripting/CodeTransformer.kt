package ch.rmy.android.http_shortcuts.scripting

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.Variables.VARIABLE_KEY_REGEX
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CodeTransformer
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
    private val variableRepository: VariableRepository,
) {
    suspend fun transformForEditing(code: String): String {
        val (shortcuts, variables) = getShortcutsAndVariables()
        return code.replaceShortcutIdsWithNames(shortcuts)
            .replaceVariableIdsWithKeys(variables)
    }

    private suspend fun getShortcutsAndVariables(): Pair<List<Shortcut>, List<Variable>> =
        coroutineScope {
            val deferredShortcuts = async {
                shortcutRepository.getShortcuts()
            }
            val deferredVariables = async {
                variableRepository.getVariables()
            }
            Pair(deferredShortcuts.await(), deferredVariables.await())
        }

    private fun String.replaceShortcutIdsWithNames(shortcuts: List<Shortcut>): String =
        SHORTCUT_PLACEHOLDER_REGEX.replace(this) { result ->
            val shortcutId = result.groupValues[1]
            val shortcut = shortcuts.find { it.id == shortcutId }
            val shortcutName = shortcut?.name?.escaped() ?: shortcutId
            "\"$shortcutName\""
        }

    private fun String.escaped(): String =
        replace("\\", "\\\\")
            .replace("\"", "\\\"")

    private fun String.replaceVariableIdsWithKeys(variables: List<Variable>): String =
        VARIABLE_PLACEHOLDER_REGEX.replace(this) { result ->
            val variableId = result.groupValues[1]
            val variable = variables.find { it.id == variableId }
            val variableKey = variable?.key ?: variableId
            "\"$variableKey\""
        }

    suspend fun transformForStoring(code: String): String {
        val (shortcuts, variables) = getShortcutsAndVariables()
        return code.replaceShortcutCallSites(shortcuts)
            .replaceVariableCallSites(variables)
    }

    private fun String.replaceShortcutCallSites(shortcuts: List<Shortcut>): String =
        SHORTCUT_CALL_SITE_REGEX.replace(this) { result ->
            val (functionName, quotedShortcutName, nextChar) = result.destructured
            val quotationMark = quotedShortcutName.substring(0, 1)
            val shortcutName = quotedShortcutName.substring(1, quotedShortcutName.length - 1)
                .replace("\\$quotationMark", quotationMark)
                .replace("\\\\", "\\")
            val shortcutId = shortcuts.find { it.name == shortcutName }?.id
            if (shortcutId != null) {
                """$functionName(/*[shortcut]*/"$shortcutId"/*[/shortcut]*/$nextChar"""
            } else {
                result.value
            }
        }

    private fun String.replaceVariableCallSites(variables: List<Variable>): String =
        VARIABLE_CALL_SITE_REGEX.replace(this) { result ->
            val (functionName, quotedVariableKey, nextChar) = result.destructured
            val variableKey = quotedVariableKey.substring(1, quotedVariableKey.length - 1)
            val variableId = variables.find { it.key == variableKey }?.id
            if (variableId != null) {
                """$functionName(/*[variable]*/"$variableId"/*[/variable]*/$nextChar"""
            } else {
                result.value
            }
        }

    fun transformForExecuting(code: String): String =
        code.replace("/*[shortcut]*/", "")
            .replace("/*[/shortcut]*/", "")
            .replace("/*[variable]*/", "")
            .replace("/*[/variable]*/", "")

    companion object {
        private val SHORTCUT_PLACEHOLDER_REGEX = """/\*\[shortcut]\*/"([^"]+)"/\*\[/shortcut]\*/""".toRegex()
        private val VARIABLE_PLACEHOLDER_REGEX = """/\*\[variable]\*/"([^"]+)"/\*\[/variable]\*/""".toRegex()
        private val VARIABLE_CALL_SITE_REGEX =
            """(getVariable|setVariable)\(("$VARIABLE_KEY_REGEX"|'$VARIABLE_KEY_REGEX')([,|)])""".toRegex()
        private val SHORTCUT_FUNCTIONS = arrayOf(
            "renameShortcut",
            "changeDescription",
            "changeIcon",
            "enqueueShortcut",
            "triggerShortcut",
            "executeShortcut",
        )
        private val SHORTCUT_CALL_SITE_REGEX =
            """(${SHORTCUT_FUNCTIONS.joinToString(separator = "|")})\((".+?(?<!\\)"|'.+?(?<!\\)')([,|)])""".toRegex()
    }
}
