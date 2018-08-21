package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.Destroyable
import ch.rmy.android.http_shortcuts.utils.color
import io.realm.RealmList

class VariablePlaceholderProvider(context: Context, private val variables: RealmList<Variable>) : Destroyable {

    private val placeholderColor by lazy {
        color(context, R.color.variable)
    }

    val placeholders
        get() = internalPlaceholders

    val constantsPlaceholders
        get() = internalPlaceholders.filter { it.isConstant }

    val hasVariables
        get() = internalPlaceholders.isNotEmpty()

    val hasConstants
        get() = constantsPlaceholders.isNotEmpty()

    private var internalPlaceholders: List<VariablePlaceholder> = emptyList()

    init {
        variables.addChangeListener { _ ->
            regenerateKeys()
        }
        regenerateKeys()
    }

    fun findPlaceholder(variableKey: String): VariablePlaceholder? =
            internalPlaceholders.firstOrNull { it.variableKey == variableKey }

    private fun regenerateKeys() {
        if (variables.isValid) {
            internalPlaceholders = variables
                    .map { VariablePlaceholder(it.key, placeholderColor, it.type == Variable.TYPE_CONSTANT) }
        }
    }

    override fun destroy() {
        if (variables.isValid) {
            variables.removeAllChangeListeners()
        }
    }

}