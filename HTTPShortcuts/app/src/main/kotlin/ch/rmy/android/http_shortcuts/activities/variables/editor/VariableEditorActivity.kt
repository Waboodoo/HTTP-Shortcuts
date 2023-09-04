package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VariableEditorActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        VariableEditorScreen(
            variableId = intent.getStringExtra(EXTRA_VARIABLE_ID),
            variableType = VariableType.parse(intent.getStringExtra(EXTRA_VARIABLE_TYPE)),
        )
    }

    class IntentBuilder(type: VariableType) : BaseIntentBuilder(VariableEditorActivity::class) {

        init {
            intent.putExtra(EXTRA_VARIABLE_TYPE, type.type)
        }

        fun variableId(variableId: VariableId) = also {
            intent.putExtra(EXTRA_VARIABLE_ID, variableId)
        }
    }

    companion object {
        private const val EXTRA_VARIABLE_ID = "ch.rmy.android.http_shortcuts.activities.variables.editor.VariableEditorActivity.variable_id"
        private const val EXTRA_VARIABLE_TYPE = "ch.rmy.android.http_shortcuts.activities.variables.editor.VariableEditorActivity.variable_type"
    }
}
