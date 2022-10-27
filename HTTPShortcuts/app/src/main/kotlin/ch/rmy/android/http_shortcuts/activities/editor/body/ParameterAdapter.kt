package ch.rmy.android.http_shortcuts.activities.editor.body

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.setText
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.body.models.ParameterListItem
import ch.rmy.android.http_shortcuts.databinding.ListEmptyItemBinding
import ch.rmy.android.http_shortcuts.databinding.ListItemParameterBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class ParameterAdapter
@Inject
constructor(
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
) :
    BaseAdapter<ParameterListItem>() {

    sealed interface UserEvent {
        data class ParameterClicked(val id: String) : UserEvent
    }

    private val userEventChannel = Channel<UserEvent>(capacity = Channel.UNLIMITED)

    val userEvents: Flow<UserEvent> = userEventChannel.receiveAsFlow()

    override fun areItemsTheSame(oldItem: ParameterListItem, newItem: ParameterListItem): Boolean =
        when (oldItem) {
            is ParameterListItem.Parameter -> (newItem as? ParameterListItem.Parameter)?.id == oldItem.id
            is ParameterListItem.EmptyState -> newItem is ParameterListItem.EmptyState
        }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is ParameterListItem.Parameter -> VIEW_TYPE_PARAMETER
            is ParameterListItem.EmptyState -> VIEW_TYPE_EMPTY_STATE
        }

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater): RecyclerView.ViewHolder? =
        when (viewType) {
            VIEW_TYPE_PARAMETER -> ParameterViewHolder(ListItemParameterBinding.inflate(layoutInflater, parent, false))
            VIEW_TYPE_EMPTY_STATE -> EmptyStateViewHolder(ListEmptyItemBinding.inflate(layoutInflater, parent, false))
            else -> null
        }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: ParameterListItem, payloads: List<Any>) {
        when (holder) {
            is ParameterViewHolder -> holder.setItem(item as ParameterListItem.Parameter)
            is EmptyStateViewHolder -> holder.setItem(item as ParameterListItem.EmptyState)
        }
    }

    inner class ParameterViewHolder(
        private val binding: ListItemParameterBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        lateinit var parameterId: String
            private set

        init {
            binding.root.setOnClickListener {
                userEventChannel.trySend(UserEvent.ParameterClicked(parameterId))
            }
        }

        private val variablePlaceholderColor by lazy(LazyThreadSafetyMode.NONE) {
            color(context, R.color.variable)
        }

        fun setItem(item: ParameterListItem.Parameter) {
            parameterId = item.id
            binding.parameterKey.text = Variables.rawPlaceholdersToVariableSpans(
                item.key,
                variablePlaceholderProvider,
                variablePlaceholderColor,
            )
            binding.parameterValue.text = item.value
                ?.let { value ->
                    Variables.rawPlaceholdersToVariableSpans(
                        value,
                        variablePlaceholderProvider,
                        variablePlaceholderColor,
                    )
                }
                ?: item.label?.localize(context)
        }
    }

    inner class EmptyStateViewHolder(
        private val binding: ListEmptyItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setItem(item: ParameterListItem.EmptyState) {
            binding.emptyMarker.setText(item.title)
            binding.emptyMarkerInstructions.setText(item.instructions)
        }
    }

    companion object {
        private const val VIEW_TYPE_PARAMETER = 1
        private const val VIEW_TYPE_EMPTY_STATE = 2
    }
}
