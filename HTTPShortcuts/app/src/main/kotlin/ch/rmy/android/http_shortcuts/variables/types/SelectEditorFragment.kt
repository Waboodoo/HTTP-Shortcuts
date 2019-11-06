package ch.rmy.android.http_shortcuts.variables.types

import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.SelectVariableOptionsAdapter
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showMessageDialog
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews
import kotterknife.bindView

class SelectEditorFragment : VariableEditorFragment() {

    private var variable: Variable? = null

    override val layoutResource = R.layout.variable_editor_select

    private val selectOptionsAddButton: Button by bindView(R.id.select_options_add_button)
    private val selectOptionsList: RecyclerView by bindView(R.id.select_options_list)
    private val optionsAdapter = SelectVariableOptionsAdapter()

    override fun setupViews() {
        selectOptionsAddButton.setOnClickListener { showAddDialog() }
        selectOptionsList.layoutManager = LinearLayoutManager(context)
        selectOptionsList.adapter = optionsAdapter
        optionsAdapter.clickListener = ::showEditDialog
        initDragOrdering()
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { variable!!.options!!.size > 1 }
        dragOrderingHelper.positionChangeSource.subscribe { (oldPosition, newPosition) ->
            variable!!.options!!.move(oldPosition, newPosition)

            optionsAdapter.notifyItemMoved(oldPosition, newPosition)
        }.attachTo(destroyer)
        dragOrderingHelper.attachTo(selectOptionsList)
    }

    override fun updateViews(variable: Variable) {
        this.variable = variable
        optionsAdapter.options = variable.options!!
        optionsAdapter.notifyDataSetChanged()
    }

    private fun showAddDialog() {
        showEditDialog(null)
    }

    private fun showEditDialog(option: Option?) {
        val destroyer = Destroyer()

        val editorView = layoutInflater.inflate(R.layout.select_option_editor_item, null)
        val labelInput = editorView.findViewById<EditText>(R.id.select_option_label)
        val valueInput = editorView.findViewById<VariableEditText>(R.id.select_option_value)
        val valueVariableButton = editorView.findViewById<VariableButton>(R.id.variable_button_value)

        bindVariableViews(valueInput, valueVariableButton, variablePlaceholderProvider)
            .attachTo(destroyer)

        if (option != null) {
            labelInput.setText(option.label)
            valueInput.rawString = option.value
        }

        DialogBuilder(context!!)
            .title(if (option != null) R.string.title_edit_select_option else R.string.title_add_select_option)
            .view(editorView)
            .positive(R.string.dialog_ok) {
                val label = labelInput.text.toString()
                val value = valueInput.rawString
                if (option != null) {
                    updateOption(option, label, value)
                } else {
                    addNewOption(label, value)
                }
            }
            .negative(R.string.dialog_cancel)
            .mapIf(option != null) {
                it.neutral(R.string.dialog_remove) { removeOption(option!!) }
            }
            .dismissListener {
                destroyer.destroy()
            }
            .showIfPossible()
    }

    private fun addNewOption(label: String, value: String) {
        val option = Option(label = label, value = value)
        variable!!.options!!.add(option)
        updateViews(variable!!)
    }

    private fun updateOption(option: Option, label: String, value: String) {
        option.label = label
        option.value = value
        updateViews(variable!!)
    }

    private fun removeOption(option: Option) {
        variable!!.options!!.removeAll { it.id == option.id }
        updateViews(variable!!)
    }

    override fun validate() = if (variable!!.options!!.isEmpty()) {
        showMessageDialog(R.string.error_not_enough_select_values)
        false
    } else {
        true
    }

}
