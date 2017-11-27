package ch.rmy.android.http_shortcuts.variables.types

import android.widget.CheckBox
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView
import java.text.SimpleDateFormat
import java.util.*

class TimeEditorFragment : VariableEditorFragment() {

    private var variable: Variable? = null

    override val layoutResource = R.layout.variable_editor_time

    val inputRememberValue: CheckBox by bindView(R.id.input_remember_value)
    val timeFormat: EditText by bindView(R.id.input_variable_time_format)

    override fun updateViews(variable: Variable) {
        this.variable = variable
        inputRememberValue.isChecked = variable.rememberValue
        timeFormat.setText(variable.dataForType?.get(TimeType.KEY_FORMAT)?.toString() ?: TimeType.DEFAULT_FORMAT)
    }

    override fun validate(): Boolean {
        try {
            SimpleDateFormat(variable?.dataForType?.get(TimeType.KEY_FORMAT)?.toString())
        } catch (e: Exception) {
            MaterialDialog.Builder(context!!)
                    .content(R.string.error_invalid_date_format)
                    .positiveText(R.string.dialog_ok)
                    .show()
            return false
        }
        return true
    }

    override fun compileIntoVariable(variable: Variable) {
        variable.rememberValue = inputRememberValue.isChecked
        val dataMap = HashMap<String, String>()
        dataMap.put(TimeType.KEY_FORMAT, timeFormat.text.toString())
        variable.dataForType = dataMap
    }

}
