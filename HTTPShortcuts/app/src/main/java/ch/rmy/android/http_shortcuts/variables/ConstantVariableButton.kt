package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.util.AttributeSet
import ch.rmy.android.http_shortcuts.R

class ConstantVariableButton : VariableButton {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun hasVariables() = variablePlaceholderProvider.hasConstants

    override fun getTitle() = R.string.dialog_title_variable_selection_constants_only

    override fun getVariables() = variablePlaceholderProvider.constantsPlaceholders

}