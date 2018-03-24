package ch.rmy.android.http_shortcuts.plugin

import android.os.Bundle
import ch.rmy.android.http_shortcuts.realm.Controller

object PluginBundleManager {

    private const val PARAM_SHORTCUT_ID = "ch.rmy.android.http_shortcuts.shortcut_id"
    private const val PARAM_VARIABLE_PREFIX = "ch.rmy.android.http_shortcuts.variables."

    fun generateBundle(shortcutId: Long, withVariables: Boolean) = Bundle().also {
        it.putLong(PARAM_SHORTCUT_ID, shortcutId)
        if (withVariables) {
            attachVariableNamesIfPossible(it)
        }
    }

    private fun attachVariableNamesIfPossible(bundle: Bundle) {
        Controller().use { controller ->
            val variableKeys = controller.getVariables().map { it.key }
            val variableParams = variableKeys.map { PluginBundleManager.PARAM_VARIABLE_PREFIX + it }
            variableKeys.forEach { variableKey ->
                bundle.putString(PluginBundleManager.PARAM_VARIABLE_PREFIX + variableKey, TaskerPlugin.VARIABLE_PREFIX + variableKey)
            }
            TaskerPlugin.Setting.setVariableReplaceKeys(bundle, variableParams.toTypedArray())
        }
    }

    fun getShortcutId(bundle: Bundle) = bundle.getLong(PARAM_SHORTCUT_ID)

    fun getVariableValues(bundle: Bundle): Map<String, String> =
            bundle
                    .keySet()
                    .filter { it.startsWith(PARAM_VARIABLE_PREFIX) }
                    .associate { it.substring(PARAM_VARIABLE_PREFIX.length) to bundle.getString(it) }
                    .filter { (variableKey, variableValue) -> variableValue != TaskerPlugin.VARIABLE_PREFIX + variableKey }

    fun isBundleValid(bundle: Bundle?) = bundle?.containsKey(PARAM_SHORTCUT_ID) ?: false

}
