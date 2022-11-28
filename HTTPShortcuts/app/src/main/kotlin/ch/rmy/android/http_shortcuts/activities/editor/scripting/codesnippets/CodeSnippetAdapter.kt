package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.ui.BaseAdapter
import ch.rmy.android.framework.ui.views.ExpandableSection
import ch.rmy.android.framework.ui.views.SimpleListItemView
import ch.rmy.android.http_shortcuts.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class CodeSnippetAdapter : BaseAdapter<ItemWrapper>() {

    sealed interface UserEvent {
        data class SectionClicked(val id: Int) : UserEvent
        data class CodeSnippetClicked(val id: Int) : UserEvent
        data class CodeSnippetAuxiliaryIconClicked(val id: Int) : UserEvent
    }

    private val userEventChannel = Channel<UserEvent>(capacity = Channel.UNLIMITED)

    val userEvents: Flow<UserEvent> = userEventChannel.receiveAsFlow()

    override fun areItemsTheSame(oldItem: ItemWrapper, newItem: ItemWrapper): Boolean =
        when (oldItem) {
            is ItemWrapper.Section -> newItem is ItemWrapper.Section && oldItem.id == newItem.id
            is ItemWrapper.CodeSnippet -> newItem is ItemWrapper.CodeSnippet && oldItem.id == newItem.id
        }

    override fun getChangePayload(oldItem: ItemWrapper, newItem: ItemWrapper): Any? =
        if (oldItem is ItemWrapper.Section && newItem is ItemWrapper.Section && oldItem.expanded != newItem.expanded) {
            ChangePayload.EXPANSION
        } else null

    override fun createViewHolder(viewType: Int, parent: ViewGroup, layoutInflater: LayoutInflater): RecyclerView.ViewHolder =
        when (viewType) {
            VIEW_TYPE_SECTION -> {
                SectionViewHolder(ExpandableSection(parent.context).applyLayoutParams())
            }
            VIEW_TYPE_CODE_SNIPPET -> {
                CodeSnippetViewHolder(SimpleListItemView(parent.context).applyLayoutParams())
            }
            else -> error("Unexpected view type $viewType")
        }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is ItemWrapper.Section -> VIEW_TYPE_SECTION
            is ItemWrapper.CodeSnippet -> VIEW_TYPE_CODE_SNIPPET
        }

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: ItemWrapper, payloads: List<Any>) {
        when (item) {
            is ItemWrapper.Section ->
                with(holder as SectionViewHolder) {
                    if (payloads.isEmpty()) {
                        setItem(item)
                    } else {
                        payloads.filterIsInstance<ChangePayload>()
                            .forEach { changePayload ->
                                when (changePayload) {
                                    ChangePayload.EXPANSION -> setExpanded(item.expanded)
                                }
                            }
                    }
                }
            is ItemWrapper.CodeSnippet -> {
                (holder as CodeSnippetViewHolder).setItem(item)
            }
        }
    }

    inner class SectionViewHolder(
        private val view: ExpandableSection,
    ) : RecyclerView.ViewHolder(view) {

        private var sectionId: Int = 0

        init {
            view.setOnClickListener {
                userEventChannel.trySend(UserEvent.SectionClicked(sectionId))
            }
        }

        fun setItem(item: ItemWrapper.Section) {
            sectionId = item.id
            view.title = item.sectionItem.title.localize(context)
            view.icon = item.sectionItem.icon
            view.expanded = item.expanded
        }

        fun setExpanded(expanded: Boolean) {
            view.expanded = expanded
        }
    }

    inner class CodeSnippetViewHolder(
        private val view: SimpleListItemView,
    ) : RecyclerView.ViewHolder(view) {

        private var codeSnippetId: Int = 0

        init {
            view.setOnClickListener {
                userEventChannel.trySend(UserEvent.CodeSnippetClicked(codeSnippetId))
            }
            view.setAuxiliaryIconClickListener {
                userEventChannel.trySend(UserEvent.CodeSnippetAuxiliaryIconClicked(codeSnippetId))
            }
        }

        fun setItem(item: ItemWrapper.CodeSnippet) {
            codeSnippetId = item.id
            view.title = item.codeSnippetItem.title.localize(context)
            view.subtitle = item.codeSnippetItem.description?.localize(context)
            view.auxiliaryIcon = if (item.codeSnippetItem.docRef != null) R.drawable.ic_info else null
        }
    }

    private enum class ChangePayload {
        EXPANSION,
    }

    companion object {
        private const val VIEW_TYPE_SECTION = 1
        private const val VIEW_TYPE_CODE_SNIPPET = 2

        internal fun <T : View> T.applyLayoutParams() = apply {
            layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
    }
}
