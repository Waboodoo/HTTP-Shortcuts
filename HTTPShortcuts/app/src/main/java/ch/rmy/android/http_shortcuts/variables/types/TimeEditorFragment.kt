package ch.rmy.android.http_shortcuts.variables.types

import android.widget.CheckBox
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.showMessageDialog
import kotterknife.bindView
import java.text.SimpleDateFormat
import java.util.*

class TimeEditorFragment : VariableEditorFragment() {

    private lateinit var variable: Variable

    override val layoutResource = R.layout.variable_editor_time

    private val inputRememberValue: CheckBox by bindView(R.id.input_remember_value)
    private val timeFormat: EditText by bindView(R.id.input_variable_time_format)

    override fun updateViews(variable: Variable) {
        this.variable = variable
        inputRememberValue.isChecked = variable.rememberValue
        timeFormat.setText(variable.dataForType[TimeType.KEY_FORMAT] ?: TimeType.DEFAULT_FORMAT)
    }

    override fun validate() =
            try {
                SimpleDateFormat(variable.dataForType[TimeType.KEY_FORMAT], Locale.US)
                true
            } catch (e: Exception) {
                showMessageDialog(R.string.error_invalid_time_format)
                false
            }

    override fun compileIntoVariable(variable: Variable) {
        variable.rememberValue = inputRememberValue.isChecked
        variable.dataForType = mapOf(TimeType.KEY_FORMAT to timeFormat.text.toString())
    }

}
