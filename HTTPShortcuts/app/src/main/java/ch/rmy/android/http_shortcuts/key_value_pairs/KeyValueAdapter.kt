package ch.rmy.android.http_shortcuts.key_value_pairs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.color
import ch.rmy.android.http_shortcuts.variables.Variables

internal class KeyValueAdapter<T : KeyValuePair>(context: Context) : ArrayAdapter<T>(context, R.layout.key_value_item) {

    var variables: List<Variable> = emptyList()
    private val variableColor by lazy {
        color(context, R.color.variable)
    }

    override fun getView(position: Int, rowView: View?, parent: ViewGroup): View {
        val parameter = getItem(position)

        val row = rowView ?: run {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.key_value_item, parent, false)
        }

        if (parameter != null) {
            // TODO: Fix the hack of appending a space character to make sure the string contains
            // at least 1 character that is not part of a variable placeholder
            val keyView = row.findViewById<TextView>(R.id.text_key)
            keyView.text = Variables.rawPlaceholdersToVariableSpans(parameter.key + " ", variables, variableColor)

            val valueView = row.findViewById<TextView>(R.id.text_value)
            valueView.text = Variables.rawPlaceholdersToVariableSpans(parameter.value + " ", variables, variableColor)
        }
        return row
    }

}