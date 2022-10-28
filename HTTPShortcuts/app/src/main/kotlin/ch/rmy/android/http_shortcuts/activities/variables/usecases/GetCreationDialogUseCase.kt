package ch.rmy.android.http_shortcuts.activities.variables.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings
import ch.rmy.android.http_shortcuts.activities.variables.VariablesViewModel
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import javax.inject.Inject

class GetCreationDialogUseCase
@Inject
constructor() {

    @CheckResult
    operator fun invoke(viewModel: VariablesViewModel) =
        createDialogState {
            title(R.string.title_select_variable_type)
                .runFor(getOptions()) { option ->
                    when (option) {
                        is VariableTypeOption.Separator -> separator()
                        is VariableTypeOption.Variable -> {
                            item(name = option.name.localize(context), description = option.description.localize(context)) {
                                viewModel.onCreationDialogVariableTypeSelected(option.type)
                            }
                        }
                    }
                }
                .build()
        }

    private fun getOptions(): List<VariableTypeOption> =
        STRUCTURED_TYPES
            .let { sections ->
                sections.fold(emptyList()) { list, section ->
                    val optionsInSection = section.map { type ->
                        val typeMapping = VariableTypeMappings.getTypeMapping(type)
                        VariableTypeOption.Variable(
                            name = StringResLocalizable(typeMapping.name),
                            type = typeMapping.type,
                            description = StringResLocalizable(typeMapping.description),
                        )
                    }
                    if (list.isNotEmpty()) {
                        list
                            .plus(VariableTypeOption.Separator)
                            .plus(optionsInSection)
                    } else {
                        optionsInSection
                    }
                }
            }

    private sealed interface VariableTypeOption {
        data class Variable(
            val type: VariableType,
            val name: Localizable,
            val description: Localizable,
        ) : VariableTypeOption

        object Separator : VariableTypeOption
    }

    companion object {
        private val STRUCTURED_TYPES
            get() = listOf(
                listOf(
                    VariableType.CONSTANT,
                ),
                listOf(
                    VariableType.SELECT,
                    VariableType.TEXT,
                    VariableType.NUMBER,
                    VariableType.SLIDER,
                    VariableType.PASSWORD,
                    VariableType.DATE,
                    VariableType.TIME,
                    VariableType.COLOR,
                ),
                listOf(
                    VariableType.TOGGLE,
                    VariableType.CLIPBOARD,
                    VariableType.UUID,
                ),
            )
    }
}
