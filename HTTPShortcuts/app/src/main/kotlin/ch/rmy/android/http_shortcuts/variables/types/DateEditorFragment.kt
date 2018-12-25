package ch.rmy.android.http_shortcuts.variables.types

import android.widget.CheckBox
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.showMessageDialog
import kotterknife.bindView
import java.text.SimpleDateFormat
import java.util.*

class DateEditorFragment : VariableEditorFragment() {

    private lateinit var variable: Variable

    override val layoutResource = R.layout.variable_editor_date

    private val inputRememberValue: CheckBox by bindView(R.id.input_remember_value)
    private val dateFormat: EditText by bindView(R.id.input_variable_date_format)

    override fun updateViews(variable: Variable) {
        this.variable = variable
        inputRememberValue.isChecked = variable.rememberValue
        dateFormat.setText(variable.dataForType[DateType.KEY_FORMAT] ?: DateType.DEFAULT_FORMAT)
    }

    override fun validate(): Boolean {
        try {
            SimpleDateFormat(variable.dataForType[DateType.KEY_FORMAT], Locale.US)
        } catch (e: Exception) {
            showMessageDialog(R.string.error_invalid_date_format)
            return false
        }
        return true
    }

    override fun compileIntoVariable(variable: Variable) {
        variable.rememberValue = inputRememberValue.isChecked
        variable.dataForType = mapOf(DateType.KEY_FORMAT to dateFormat.text.toString())
    }

}
