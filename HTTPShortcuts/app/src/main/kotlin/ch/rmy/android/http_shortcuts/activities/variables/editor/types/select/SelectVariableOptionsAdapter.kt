package ch.rmy.android.http_shortcuts.activities.variables.editor.types.select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.databinding.SelectOptionBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class SelectVariableOptionsAdapter : BaseAdapter<OptionItem>() {

    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    sealed interface UserEvent {
        data class OptionClicked(val id: String) : UserEvent
    }

    private val userEventSubject = PublishSubject.create<UserEvent>()

    val userEvents: Observable<UserEvent>
        get() = userEventSubject

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater) =
        SelectOptionViewHolder(SelectOptionBinding.inflate(layoutInflater, parent, false))

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: OptionItem, payloads: List<Any>) {
        (holder as SelectOptionViewHolder).setItem(item)
    }

    override fun areItemsTheSame(oldItem: OptionItem, newItem: OptionItem) =
        oldItem.id == newItem.id

    inner class SelectOptionViewHolder(
        private val binding: SelectOptionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private val variablePlaceholderColor by lazy {
            color(context, R.color.variable)
        }

        lateinit var optionId: String
            private set

        init {
            binding.root.setOnClickListener {
                userEventSubject.onNext(UserEvent.OptionClicked(optionId))
            }
        }

        fun setItem(item: OptionItem) {
            optionId = item.id
            binding.selectOptionLabel.text = Variables.rawPlaceholdersToVariableSpans(
                item.text,
                variablePlaceholderProvider,
                variablePlaceholderColor,
            )
        }
    }
}
