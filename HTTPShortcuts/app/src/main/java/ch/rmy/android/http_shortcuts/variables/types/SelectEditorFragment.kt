package ch.rmy.android.http_shortcuts.variables.types

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Option
import ch.rmy.android.http_shortcuts.realm.models.Variable
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView

class SelectEditorFragment : VariableEditorFragment() {

    private var variable: Variable? = null

    override val layoutResource = R.layout.variable_editor_select

    val selectOptionsAddButton: Button by bindView(R.id.select_options_add_button)
    val selectOptionsList: LinearLayout by bindView(R.id.select_options_list)

    override fun setupViews(parent: View) {
        selectOptionsAddButton.setOnClickListener { showEditDialog(null, -1) }
    }

    override fun updateViews(variable: Variable) {
        this.variable = variable
        selectOptionsList.removeAllViews()
        var i = 0
        for (option in variable.options!!) {
            selectOptionsList.addView(createOptionView(option, i))
            i++
        }
    }

    private fun createOptionView(option: Option, index: Int): View {
        val optionView = getLayoutInflater(null).inflate(R.layout.select_option, selectOptionsList, false)
        (optionView.findViewById(R.id.select_option_label) as TextView).text = option.label
        optionView.setOnClickListener { showEditDialog(option, index) }
        return optionView
    }

    private fun showEditDialog(option: Option?, index: Int) {
        val editorView = getLayoutInflater(null).inflate(R.layout.select_option_editor_item, null)
        val labelInput = editorView.findViewById(R.id.select_option_label) as TextView
        val valueInput = editorView.findViewById(R.id.select_option_value) as TextView

        if (option != null) {
            labelInput.text = option.label
            valueInput.text = option.value
        }
        var builder: MaterialDialog.Builder = MaterialDialog.Builder(context)
                .title(R.string.title_add_select_option)
                .customView(editorView, true)
                .positiveText(R.string.dialog_ok)
                .onPositive { _, _ ->
                    val label = labelInput.text.toString()
                    val value = valueInput.text.toString()
                    if (option != null) {
                        updateOption(option, label, value)
                    } else {
                        addNewOption(label, value)
                    }
                }
                .negativeText(R.string.dialog_cancel)

        if (option != null) {
            builder = builder
                    .neutralText(R.string.dialog_remove)
                    .onNeutral { _, _ -> removeOption(index) }
        }

        builder.show()
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

    override fun validate(): Boolean {
        return if (variable!!.options!!.isEmpty()) {
            MaterialDialog.Builder(context)
                    .content(R.string.error_not_enough_select_values)
                    .positiveText(R.string.dialog_ok)
                    .show()
            false
        } else {
            true
        }
    }

}
