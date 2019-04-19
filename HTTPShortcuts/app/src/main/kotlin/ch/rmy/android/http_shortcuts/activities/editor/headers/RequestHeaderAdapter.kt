package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseAdapter
import ch.rmy.android.http_shortcuts.activities.BaseViewHolder
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import kotterknife.bindView

class RequestHeaderAdapter(context: Context, headers: ListLiveData<Header>, val variablePlaceholderProvider: VariablePlaceholderProvider) : BaseAdapter<Header>(context, headers) {

    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }

    override val emptyMarker = EmptyMarker(
        context.getString(R.string.empty_state_request_headers),
        context.getString(R.string.empty_state_request_headers_instructions)
    )

    override fun createViewHolder(parentView: ViewGroup) = HeaderViewHolder(parentView)

    inner class HeaderViewHolder(parent: ViewGroup) : BaseViewHolder<Header>(parent, R.layout.list_item_header, this@RequestHeaderAdapter) {

        private val headerKey: TextView by bindView(R.id.header_key)
        private val headerValue: TextView by bindView(R.id.header_value)

        override fun updateViews(item: Header) {
            headerKey.text = Variables.rawPlaceholdersToVariableSpans(
                item.key,
                variablePlaceholderProvider,
                variablePlaceholderColor
            )
            headerValue.text = Variables.rawPlaceholdersToVariableSpans(
                item.value,
                variablePlaceholderProvider,
                variablePlaceholderColor
            )
        }

    }

}
