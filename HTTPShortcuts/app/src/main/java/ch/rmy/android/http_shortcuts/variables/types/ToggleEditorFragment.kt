package ch.rmy.android.http_shortcuts.variables.types

import android.view.View
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Option
import ch.rmy.android.http_shortcuts.realm.models.Variable
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.variable_editor_toggle.*

class ToggleEditorFragment : VariableEditorFragment() {

    private var variable: Variable? = null

    override val layoutResource = R.layout.variable_editor_toggle

    override fun setupViews(parent: View) {
        toggle_options_add_button.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        MaterialDialog.Builder(context)
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
        toggle_options_list.removeAllViews()
        var i = 0
        for (option in variable.options!!) {
            toggle_options_list.addView(createOptionView(option, i))
            i++
        }
    }

    private fun createOptionView(option: Option, index: Int): View {
        val optionView = getLayoutInflater(null).inflate(R.layout.toggle_option, toggle_options_list, false)
        (optionView.findViewById(R.id.toggle_option_value) as TextView).text = option.value
        optionView.setOnClickListener { showEditDialog(option, index) }
        return optionView
    }

    private fun showEditDialog(option: Option, index: Int) {
        MaterialDialog.Builder(context)
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
            MaterialDialog.Builder(context)
                    .content(R.string.error_not_enough_toggle_values)
                    .positiveText(R.string.dialog_ok)
                    .show()
            return false
        }
        return true
    }

}
