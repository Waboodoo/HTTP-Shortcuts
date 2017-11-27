package ch.rmy.android.http_shortcuts.variables.types

import android.widget.CheckBox
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView
import java.text.SimpleDateFormat
import java.util.*

class DateEditorFragment : VariableEditorFragment() {

    private var variable: Variable? = null

    override val layoutResource = R.layout.variable_editor_date

    val inputRememberValue: CheckBox by bindView(R.id.input_remember_value)
    val dateFormat: EditText by bindView(R.id.input_variable_date_format)

    override fun updateViews(variable: Variable) {
        this.variable = variable
        inputRememberValue.isChecked = variable.rememberValue
        dateFormat.setText(variable.dataForType?.get(DateType.KEY_FORMAT)?.toString() ?: DateType.DEFAULT_FORMAT)
    }

    override fun validate(): Boolean {
        try {
            SimpleDateFormat(variable?.dataForType?.get(DateType.KEY_FORMAT)?.toString())
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
        dataMap.put(DateType.KEY_FORMAT, dateFormat.text.toString())
        variable.dataForType = dataMap
    }

}
