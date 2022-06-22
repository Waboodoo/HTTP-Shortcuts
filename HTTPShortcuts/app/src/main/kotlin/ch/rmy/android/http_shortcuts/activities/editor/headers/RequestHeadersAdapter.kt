package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.setText
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.databinding.ListEmptyItemBinding
import ch.rmy.android.http_shortcuts.databinding.ListItemHeaderBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class RequestHeadersAdapter
@Inject
constructor(
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
) :
    BaseAdapter<HeaderListItem>() {

    sealed interface UserEvent {
        data class HeaderClicked(val id: String) : UserEvent
    }

    private val userEventSubject = PublishSubject.create<UserEvent>()

    val userEvents: Observable<UserEvent>
        get() = userEventSubject

    override fun areItemsTheSame(oldItem: HeaderListItem, newItem: HeaderListItem): Boolean =
        when (oldItem) {
            is HeaderListItem.Header -> (newItem as? HeaderListItem.Header)?.id == oldItem.id
            is HeaderListItem.EmptyState -> newItem is HeaderListItem.EmptyState
        }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is HeaderListItem.Header -> VIEW_TYPE_HEADER
            is HeaderListItem.EmptyState -> VIEW_TYPE_EMPTY_STATE
        }

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater): RecyclerView.ViewHolder? =
        when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(ListItemHeaderBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_EMPTY_STATE -> EmptyStateViewHolder(ListEmptyItemBinding.inflate(layoutInflater, parent, false))
            else -> null
        }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: HeaderListItem, payloads: List<Any>) {
        when (holder) {
            is HeaderViewHolder -> holder.setItem(item as HeaderListItem.Header)
            is EmptyStateViewHolder -> holder.setItem(item as HeaderListItem.EmptyState)
        }
    }

    inner class HeaderViewHolder(
        private val binding: ListItemHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var headerId: String
            private set

        init {
            binding.root.setOnClickListener {
                userEventSubject.onNext(UserEvent.HeaderClicked(headerId))
            }
        }

        private val variablePlaceholderColor by lazy {
            color(context, R.color.variable)
        }

        fun setItem(item: HeaderListItem.Header) {
            headerId = item.id
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

    inner class EmptyStateViewHolder(
        private val binding: ListEmptyItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setItem(item: HeaderListItem.EmptyState) {
            binding.emptyMarker.setText(item.title)
            binding.emptyMarkerInstructions.setText(item.instructions)
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_EMPTY_STATE = 2
    }
}
