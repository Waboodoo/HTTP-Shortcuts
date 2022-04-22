package ch.rmy.android.http_shortcuts.activities.variables.editor.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings
import ch.rmy.android.http_shortcuts.activities.variables.VariablesViewModel
import ch.rmy.android.http_shortcuts.data.enums.VariableType

class GetCreationDialogUseCase {

    @CheckResult
    operator fun invoke(viewModel: VariablesViewModel) =
        DialogState.create {
            title(R.string.title_select_variable_type)
                .runFor(getOptions()) { option ->
                    when (option) {
                        is VariableTypeOption.Separator -> separator()
                        is VariableTypeOption.Variable -> {
                            item(name = option.name.localize(context)) {
                                viewModel.onCreationDialogVariableTypeSelected(option.type)
                            }
                        }
                    }
                }
                .build()
        }

    private fun getOptions(): List<VariableTypeOption> =
        VariableTypeMappings.TYPES
            .flatMap { typeMapping ->
                listOf<VariableTypeOption>(
                    VariableTypeOption.Variable(
                        name = StringResLocalizable(typeMapping.name),
                        type = typeMapping.type,
                    )
                )
                    .runIf(typeMapping.type == VariableType.CONSTANT) {
                        plusElement(VariableTypeOption.Separator)
                    }
            }

    private sealed interface VariableTypeOption {
        data class Variable(val type: VariableType, val name: Localizable) : VariableTypeOption
        object Separator : VariableTypeOption
    }
}
