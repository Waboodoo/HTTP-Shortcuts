package ch.rmy.android.http_shortcuts.activities.variables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.setText
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.databinding.ListEmptyItemBinding
import ch.rmy.android.http_shortcuts.databinding.ListItemVariableBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class VariableAdapter : BaseAdapter<VariableListItem>() {

    sealed interface UserEvent {
        data class VariableClicked(val id: String) : UserEvent
    }

    private val userEventSubject = PublishSubject.create<UserEvent>()

    val userEvents: Observable<UserEvent>
        get() = userEventSubject

    override fun areItemsTheSame(oldItem: VariableListItem, newItem: VariableListItem): Boolean =
        when (oldItem) {
            is VariableListItem.Variable -> (newItem as? VariableListItem.Variable)?.id == oldItem.id
            is VariableListItem.EmptyState -> newItem is VariableListItem.EmptyState
        }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is VariableListItem.Variable -> VIEW_TYPE_VARIABLE
            is VariableListItem.EmptyState -> VIEW_TYPE_EMPTY_STATE
        }

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater): RecyclerView.ViewHolder? =
        when (viewType) {
            VIEW_TYPE_VARIABLE -> VariableViewHolder(ListItemVariableBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_EMPTY_STATE -> EmptyStateViewHolder(ListEmptyItemBinding.inflate(layoutInflater, parent, false))
            else -> null
        }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: VariableListItem, payloads: List<Any>) {
        when (holder) {
            is VariableViewHolder -> holder.setItem(item as VariableListItem.Variable)
            is EmptyStateViewHolder -> holder.setItem(item as VariableListItem.EmptyState)
        }
    }

    inner class VariableViewHolder(
        private val binding: ListItemVariableBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var variableId: VariableId
            private set

        init {
            binding.root.setOnClickListener {
                userEventSubject.onNext(UserEvent.VariableClicked(variableId))
            }
        }

        fun setItem(item: VariableListItem.Variable) {
            variableId = item.id
            binding.name.text = item.key
            binding.type.setText(item.type)
            binding.unused.isVisible = item.isUnused
        }
    }

    inner class EmptyStateViewHolder(
        private val binding: ListEmptyItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setItem(item: VariableListItem.EmptyState) {
            binding.emptyMarker.setText(item.title)
            binding.emptyMarkerInstructions.setText(item.instructions)
        }
    }

    companion object {
        private const val VIEW_TYPE_VARIABLE = 1
        private const val VIEW_TYPE_EMPTY_STATE = 2
    }
}
