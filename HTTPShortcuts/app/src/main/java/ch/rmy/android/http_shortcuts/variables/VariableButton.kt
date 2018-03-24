package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.VariablesActivity
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.EventSource
import ch.rmy.android.http_shortcuts.utils.destroyer
import ch.rmy.android.http_shortcuts.utils.mapFor
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog


class VariableButton : ImageButton {

    private val controller = destroyer.own(Controller())
    private val variables by lazy { controller.getVariables() }

    val variableSource = EventSource<Variable>()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setOnClickListener {
            if (variables.isEmpty()) {
                openInstructionDialog()
            } else {
                openVariableSelectionDialog()
            }
        }
    }

    private fun openInstructionDialog() {
        MaterialDialog.Builder(context)
                .title(R.string.help_title_variables)
                .content(R.string.help_text_variable_button)
                .positiveText(android.R.string.ok)
                .neutralText(R.string.button_create_first_variable)
                .onNeutral { _, _ -> openVariableEditor() }
                .show()
    }

    private fun openVariableEditor() {
        val intent = VariablesActivity.IntentBuilder(context)
                .build()
        context.startActivity(intent)
    }

    private fun openVariableSelectionDialog() {
        MenuDialogBuilder(context)
                .title(R.string.dialog_title_variable_selection)
                .mapFor(variables) { builder, variable ->
                    builder.item(variable.key) {
                        variableSource.notifyObservers(variable)
                    }
                }
                .toDialogBuilder()
                .neutralText(R.string.label_edit_variables)
                .onNeutral { _, _ -> openVariableEditor() }
                .showIfPossible()
    }

}