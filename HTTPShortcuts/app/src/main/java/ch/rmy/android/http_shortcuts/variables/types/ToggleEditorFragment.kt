package ch.rmy.android.http_shortcuts.variables.types

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Option
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.color
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.android.http_shortcuts.utils.showMessageDialog
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.Variables
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView

class ToggleEditorFragment : VariableEditorFragment() {

    private var variable: Variable? = null
    private val variableColor by lazy {
        color(context!!, R.color.variable)
    }

    override val layoutResource = R.layout.variable_editor_toggle

    private val toggleOptionsAddButton: Button by bindView(R.id.toggle_options_add_button)
    private val toggleOptionsList: LinearLayout by bindView(R.id.toggle_options_list)

    override fun setupViews() {
        toggleOptionsAddButton.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        showEditDialog(null, -1)
    }

    private fun showEditDialog(option: Option?, index: Int) {
        val destroyer = Destroyer()

        val editorView = layoutInflater.inflate(R.layout.toggle_option_editor_item, null)
        val valueInput = editorView.findViewById<VariableEditText>(R.id.toggle_option_value)
        val valueVariableButton = editorView.findViewById<VariableButton>(R.id.variable_button_value)

        valueInput.bind(valueVariableButton, variables).attachTo(destroyer)

        if (option != null) {
            valueInput.rawString = option.value
        }

        MaterialDialog.Builder(context!!)
                .title(if (option != null) R.string.title_edit_toggle_option else R.string.title_add_toggle_option)
                .customView(editorView, true)
                .positiveText(R.string.dialog_ok)
                .onPositive { _, _ ->
                    val value = valueInput.rawString
                    if (option != null) {
                        updateOption(option, valueInput.rawString)
                    } else {
                        addNewOption(valueInput.rawString)
                    }
                }
                .negativeText(R.string.dialog_cancel)
                .mapIf(option != null) {
                    it
                            .neutralText(R.string.dialog_remove)
                            .onNeutral { _, _ -> removeOption(index) }
                }
                .dismissListener {
                    destroyer.destroy()
                }
                .showIfPossible()
    }

    private fun addNewOption(value: String) {
        val option = Option.createNew(value, value)
        variable!!.options!!.add(option)
        updateViews(variable!!)
    }

    override fun updateViews(variable: Variable) {
        this.variable = variable
        toggleOptionsList.removeAllViews()
        variable.options!!.forEachIndexed { i, option ->
            toggleOptionsList.addView(createOptionView(option, i))
        }
    }

    private fun createOptionView(option: Option, index: Int): View {
        val optionView = layoutInflater.inflate(R.layout.toggle_option, toggleOptionsList, false)
        optionView.findViewById<TextView>(R.id.toggle_option_value).text = Variables.rawPlaceholdersToVariableSpans(option.value, variables, variableColor)
        optionView.setOnClickListener { showEditDialog(option, index) }
        return optionView
    }

    private fun updateOption(option: Option, value: String) {
        option.value = value
        updateViews(variable!!)
    }

    private fun removeOption(index: Int) {
        variable!!.options!!.removeAt(index)
        updateViews(variable!!)
    }

    override fun validate(): Boolean {
        if (variable!!.options!!.size < 2) {
            showMessageDialog(R.string.error_not_enough_toggle_values)
            return false
        }
        return true
    }

}
