package ch.rmy.android.http_shortcuts.variables.types

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Option
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.android.http_shortcuts.utils.showMessageDialog
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView

class SelectEditorFragment : VariableEditorFragment() {

    private var variable: Variable? = null

    override val layoutResource = R.layout.variable_editor_select

    private val selectOptionsAddButton: Button by bindView(R.id.select_options_add_button)
    private val selectOptionsList: LinearLayout by bindView(R.id.select_options_list)

    override fun setupViews() {
        selectOptionsAddButton.setOnClickListener { showAddDialog() }
    }

    override fun updateViews(variable: Variable) {
        this.variable = variable
        selectOptionsList.removeAllViews()
        variable.options!!.forEachIndexed { i, option ->
            selectOptionsList.addView(createOptionView(option, i))
        }
    }

    private fun createOptionView(option: Option, index: Int): View {
        val optionView = layoutInflater.inflate(R.layout.select_option, selectOptionsList, false)
        optionView.findViewById<TextView>(R.id.select_option_label).text = option.label
        optionView.setOnClickListener { showEditDialog(option, index) }
        return optionView
    }

    private fun showAddDialog() {
        showEditDialog(null, -1)
    }

    private fun showEditDialog(option: Option?, index: Int) {
        val destroyer = Destroyer()

        val editorView = layoutInflater.inflate(R.layout.select_option_editor_item, null)
        val labelInput = editorView.findViewById<EditText>(R.id.select_option_label)
        val valueInput = editorView.findViewById<VariableEditText>(R.id.select_option_value)
        val valueVariableButton = editorView.findViewById<VariableButton>(R.id.variable_button_value)

        valueInput.bind(valueVariableButton, variables).attachTo(destroyer)

        if (option != null) {
            labelInput.setText(option.label)
            valueInput.rawString = option.value
        }

        MaterialDialog.Builder(context!!)
                .title(if (option != null) R.string.title_edit_select_option else R.string.title_add_select_option)
                .customView(editorView, true)
                .positiveText(R.string.dialog_ok)
                .onPositive { _, _ ->
                    val label = labelInput.text.toString()
                    val value = valueInput.rawString
                    if (option != null) {
                        updateOption(option, label, value)
                    } else {
                        addNewOption(label, value)
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

    private fun addNewOption(label: String, value: String) {
        val option = Option.createNew(label, value)
        variable!!.options!!.add(option)
        updateViews(variable!!)
    }

    private fun updateOption(option: Option, label: String, value: String) {
        option.label = label
        option.value = value
        updateViews(variable!!)
    }

    private fun removeOption(index: Int) {
        variable!!.options!!.removeAt(index)
        updateViews(variable!!)
    }

    override fun validate() = if (variable!!.options!!.isEmpty()) {
        showMessageDialog(R.string.error_not_enough_select_values)
        false
    } else {
        true
    }

}
