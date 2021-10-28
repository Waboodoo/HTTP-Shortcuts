package ch.rmy.android.http_shortcuts.activities.editor.body

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseAdapter
import ch.rmy.android.http_shortcuts.activities.BaseViewHolder
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.databinding.ListItemParameterBinding
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables

class ParameterAdapter(context: Context, parameters: ListLiveData<Parameter>, val variablePlaceholderProvider: VariablePlaceholderProvider) :
    BaseAdapter<Parameter>(context, parameters) {

    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }

    override val emptyMarker = EmptyMarker(
        context.getString(R.string.empty_state_request_parameters),
        context.getString(R.string.empty_state_request_parameters_instructions),
    )

    override fun createViewHolder(parentView: ViewGroup) =
        ParameterViewHolder(ListItemParameterBinding.inflate(LayoutInflater.from(parentView.context), parentView, false))

    inner class ParameterViewHolder(private val binding: ListItemParameterBinding) :
        BaseViewHolder<Parameter>(binding.root, this@ParameterAdapter) {

        override fun updateViews(item: Parameter) {
            binding.parameterKey.text = Variables.rawPlaceholdersToVariableSpans(
                item.key,
                variablePlaceholderProvider,
                variablePlaceholderColor,
            )
            binding.parameterValue.text = getParameterValue(item)
        }

        private fun getParameterValue(parameter: Parameter): CharSequence =
            when {
                parameter.isFileParameter -> {
                    context.getString(R.string.subtitle_parameter_value_file)
                }
                parameter.isFilesParameter -> {
                    context.getString(R.string.subtitle_parameter_value_files)
                }
                else -> Variables.rawPlaceholdersToVariableSpans(
                    parameter.value,
                    variablePlaceholderProvider,
                    variablePlaceholderColor,
                )
            }

    }

}
