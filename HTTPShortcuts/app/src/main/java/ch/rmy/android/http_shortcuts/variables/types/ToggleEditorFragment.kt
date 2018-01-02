package ch.rmy.android.http_shortcuts.variables.types

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Option
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.showMessageDialog
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView

class ToggleEditorFragment : VariableEditorFragment() {

    private var variable: Variable? = null

    override val layoutResource = R.layout.variable_editor_toggle

    private val toggleOptionsAddButton: Button by bindView(R.id.toggle_options_add_button)
    private val toggleOptionsList: LinearLayout by bindView(R.id.toggle_options_list)

    override fun setupViews() {
        toggleOptionsAddButton.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        MaterialDialog.Builder(context!!)
                .title(R.string.title_add_toggle_option)
                .input(null, null) { _, input -> addNewOption(input.toString()) }
                .show()
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
        optionView.findViewById<TextView>(R.id.toggle_option_value).text = option.value
        optionView.setOnClickListener { showEditDialog(option, index) }
        return optionView
    }

    private fun showEditDialog(option: Option, index: Int) {
        MaterialDialog.Builder(context!!)
                .title(R.string.title_add_toggle_option)
                .input(null, option.value) { _, input -> updateOption(option, input.toString()) }
                .neutralText(R.string.dialog_remove)
                .onNeutral { _, _ -> removeOption(index) }
                .show()
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
