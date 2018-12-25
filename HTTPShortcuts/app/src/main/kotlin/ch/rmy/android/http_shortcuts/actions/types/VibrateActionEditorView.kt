package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.widget.CheckBox
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.fix
import com.satsuware.usefulviews.LabelledSpinner
import kotterknife.bindView

class VibrateActionEditorView(context: Context, private val action: VibrateAction) : BaseActionEditorView(context, R.layout.action_editor_vibrate) {

    private val patternSpinner: LabelledSpinner by bindView(R.id.input_vibration_pattern)
    private val waitForCompleteCheckbox: CheckBox by bindView(R.id.input_wait_for_vibration_complete)

    init {
        patternSpinner.fix()
        patternSpinner.setItemsArray(VibrateAction.getPatterns().map { it.getDescription(context) })
        patternSpinner.setSelection(action.patternId)
        waitForCompleteCheckbox.isChecked = action.waitForCompletion
    }

    override fun compile(): Boolean {
        action.patternId = patternSpinner.spinner.selectedItemPosition
        action.waitForCompletion = waitForCompleteCheckbox.isChecked
        return true
    }

}