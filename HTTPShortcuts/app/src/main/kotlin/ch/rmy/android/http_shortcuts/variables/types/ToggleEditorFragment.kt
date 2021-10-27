package ch.rmy.android.http_shortcuts.variables.types

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.ToggleVariableOptionsAdapter
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.databinding.VariableEditorToggleBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.mapIfNotNull
import ch.rmy.android.http_shortcuts.extensions.showMessageDialog
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews

class ToggleEditorFragment : VariableEditorFragment<VariableEditorToggleBinding>() {

    private var variable: Variable? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorToggleBinding.inflate(inflater, container, false)

    private val optionsAdapter = ToggleVariableOptionsAdapter()

    override fun setupViews() {
        optionsAdapter.variablePlaceholderProvider = variablePlaceholderProvider

        binding.toggleOptionsAddButton.setOnClickListener { showAddDialog() }
        binding.toggleOptionsList.layoutManager = LinearLayoutManager(context)
        binding.toggleOptionsList.adapter = optionsAdapter
        optionsAdapter.clickListener = ::showEditDialog
        initDragOrdering()
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { variable!!.options!!.size > 1 }
        dragOrderingHelper.positionChangeSource.subscribe { (oldPosition, newPosition) ->
            variable!!.options!!.move(oldPosition, newPosition)
            optionsAdapter.notifyItemMoved(oldPosition, newPosition)
        }.attachTo(destroyer)
        dragOrderingHelper.attachTo(binding.toggleOptionsList)
    }

    private fun showAddDialog() {
        showEditDialog(null)
    }

    private fun showEditDialog(option: Option?) {
        val destroyer = Destroyer()

        val editorView = layoutInflater.inflate(R.layout.toggle_option_editor_item, null)
        val valueInput = editorView.findViewById<VariableEditText>(R.id.toggle_option_value)
        val valueVariableButton = editorView.findViewById<VariableButton>(R.id.variable_button_value)
        bindVariableViews(valueInput, valueVariableButton, variablePlaceholderProvider)
            .attachTo(destroyer)

        if (option != null) {
            valueInput.rawString = option.value
        }

        DialogBuilder(requireContext())
            .title(if (option != null) R.string.title_edit_toggle_option else R.string.title_add_toggle_option)
            .view(editorView)
            .positive(R.string.dialog_ok) {
                val value = valueInput.rawString
                if (option != null) {
                    updateOption(option, value)
                } else {
                    addNewOption(value)
                }
            }
            .negative(R.string.dialog_cancel)
            .mapIfNotNull(option) { persistedOption ->
                neutral(R.string.dialog_remove) { removeOption(persistedOption) }
            }
            .dismissListener {
                destroyer.destroy()
            }
            .showIfPossible()
    }

    private fun addNewOption(value: String) {
        val option = Option(label = value, value = value)
        variable!!.options!!.add(option)
        updateViews(variable!!)
    }

    override fun updateViews(variable: Variable) {
        this.variable = variable
        optionsAdapter.options = variable.options!!
        optionsAdapter.notifyDataSetChanged()
    }

    private fun updateOption(option: Option, value: String) {
        option.value = value
        updateViews(variable!!)
    }

    private fun removeOption(option: Option) {
        variable!!.options!!.removeAll { it.id == option.id }
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
