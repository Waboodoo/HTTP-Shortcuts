package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import kotterknife.bindView

class ExtractHeaderActionEditorView(
        context: Context,
        private val action: ExtractHeaderAction,
        variablePlaceholderProvider: VariablePlaceholderProvider
) : BaseActionEditorView(context, R.layout.action_editor_extract_header) {

    private val headerKeyView: AutoCompleteTextView by bindView(R.id.input_header_key)
    private val targetVariableView: TextView by bindView(R.id.target_variable)
    private val variableButton: VariableButton by bindView(R.id.variable_button_target_variable)

    private var selectedVariableKey: String = action.variableKey

    init {
        headerKeyView.setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, SUGGESTED_KEYS))
        headerKeyView.setText(action.headerKey)

        targetVariableView.text = action.variableKey
        targetVariableView.setOnClickListener {
            variableButton.performClick()
        }
        variableButton.variablePlaceholderProvider = variablePlaceholderProvider
        variableButton.variableSource.add {
            selectedVariableKey = it.variableKey
            updateViews()
        }.attachTo(destroyer)
        updateViews()
    }

    private fun updateViews() {
        if (selectedVariableKey.isEmpty()) {
            targetVariableView.setText(R.string.action_type_target_variable_no_variable_selected)
        } else {
            targetVariableView.text = selectedVariableKey
        }
    }

    override fun compile(): Boolean {
        val headerKey = headerKeyView.text.toString()
        if (selectedVariableKey.isEmpty() || headerKey.isEmpty()) {
            return false
        }
        action.headerKey = headerKey
        action.variableKey = selectedVariableKey
        return true
    }

    companion object {

        val SUGGESTED_KEYS = arrayOf(
                "Age",
                "Allow",
                "Cache-Control",
                "Content-Disposition",
                "Content-Encoding",
                "Content-Language",
                "Content-Length",
                "Content-Location",
                "Content-MD5",
                "Content-Range",
                "Content-Type",
                "Date",
                "ETag",
                "Expires",
                "Last-Modified",
                "Link",
                "Location",
                "Pragma",
                "Refresh",
                "Retry-After",
                "Server",
                "Set-Cookie",
                "Trailer",
                "Transfer-Encoding",
                "Upgrade",
                "Via",
                "Warning",
                "WWW-Authenticate"
        )

    }

}