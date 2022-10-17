package ch.rmy.android.http_shortcuts.activities.variables.editor.types.toggle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.databinding.ToggleOptionBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class ToggleVariableOptionsAdapter
@Inject
constructor(
    private val variablePlaceholderProvider: VariablePlaceholderProvider,
) : BaseAdapter<OptionItem>() {

    sealed interface UserEvent {
        data class OptionClicked(val id: String) : UserEvent
    }

    private val userEventChannel = Channel<UserEvent>(capacity = Channel.UNLIMITED)

    val userEvents: Flow<UserEvent> = userEventChannel.receiveAsFlow()

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater) =
        SelectOptionViewHolder(ToggleOptionBinding.inflate(layoutInflater, parent, false))

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: OptionItem, payloads: List<Any>) {
        (holder as SelectOptionViewHolder).setItem(item)
    }

    override fun areItemsTheSame(oldItem: OptionItem, newItem: OptionItem) =
        oldItem.id == newItem.id

    inner class SelectOptionViewHolder(
        private val binding: ToggleOptionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private val variablePlaceholderColor by lazy(LazyThreadSafetyMode.NONE) {
            color(context, R.color.variable)
        }

        lateinit var optionId: String
            private set

        init {
            binding.root.setOnClickListener {
                userEventChannel.trySend(UserEvent.OptionClicked(optionId))
            }
        }

        fun setItem(item: OptionItem) {
            optionId = item.id
            binding.toggleOptionValue.text = Variables.rawPlaceholdersToVariableSpans(
                item.text,
                variablePlaceholderProvider,
                variablePlaceholderColor,
            )
        }
    }
}
