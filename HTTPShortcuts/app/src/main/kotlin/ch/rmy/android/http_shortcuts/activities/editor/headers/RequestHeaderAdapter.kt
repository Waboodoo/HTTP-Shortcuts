package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseAdapter
import ch.rmy.android.http_shortcuts.activities.BaseViewHolder
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.databinding.ListItemHeaderBinding
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables

class RequestHeaderAdapter(context: Context, headers: ListLiveData<Header>, val variablePlaceholderProvider: VariablePlaceholderProvider) :
    BaseAdapter<Header>(context, headers) {

    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }

    override val emptyMarker = EmptyMarker(
        context.getString(R.string.empty_state_request_headers),
        context.getString(R.string.empty_state_request_headers_instructions),
    )

    override fun createViewHolder(parentView: ViewGroup) =
        HeaderViewHolder(ListItemHeaderBinding.inflate(LayoutInflater.from(parentView.context), parentView, false))

    inner class HeaderViewHolder(
        private val binding: ListItemHeaderBinding,
    ) : BaseViewHolder<Header>(binding.root, this@RequestHeaderAdapter) {

        override fun updateViews(item: Header) {
            binding.headerKey.text = Variables.rawPlaceholdersToVariableSpans(
                item.key,
                variablePlaceholderProvider,
                variablePlaceholderColor,
            )
            binding.headerValue.text = Variables.rawPlaceholdersToVariableSpans(
                item.value,
                variablePlaceholderProvider,
                variablePlaceholderColor,
            )
        }
    }
}
