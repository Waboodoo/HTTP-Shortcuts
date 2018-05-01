package ch.rmy.android.http_shortcuts.actions

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.actions.types.BaseAction
import ch.rmy.android.http_shortcuts.actions.types.VibrateAction
import ch.rmy.android.http_shortcuts.adapters.ActionListAdapter
import ch.rmy.android.http_shortcuts.utils.Destroyable
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import kotterknife.bindView

class ActionsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), Destroyable {

    var actions: List<ActionDTO>
        get() = internalActions.map { it.action }
        set(value) {
            internalActions.clear()
            value.mapTo(internalActions) { ActionFactory.fromDTO(it) }
            adapter.actions = internalActions
        }

    private var internalActions = mutableListOf<BaseAction>()

    private val addButton: Button by bindView(R.id.action_add_button)
    private val actionList: RecyclerView by bindView(R.id.action_list)
    private val adapter = ActionListAdapter(context)
    private val destroyer = Destroyer()

    init {
        inflate(context, R.layout.action_list, this)
        actionList.layoutManager = LinearLayoutManager(context)
        actionList.adapter = adapter

        adapter.clickListener = this::deleteAction

        addButton.setOnClickListener { openAddDialog() }

        initDragOrdering()
    }

    private fun openAddDialog() {
        // TODO
        //internalActions.add(ActionFactory.fromDTO(ActionDTO(type = VibrateAction.TYPE)))
        internalActions.add(ActionFactory.fromDTO(ActionDTO(type = VibrateAction.TYPE, data = mapOf(
                "pattern" to "2",
                "wait" to "true"
        ))))
        adapter.notifyDataSetChanged()
    }

    private fun deleteAction(action: BaseAction) {
        internalActions.removeAll { it.id == action.id }
        adapter.notifyDataSetChanged()
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper()
        dragOrderingHelper.attachTo(actionList)
        dragOrderingHelper.positionChangeSource.add { (oldPosition, newPosition) ->
            val action = internalActions.removeAt(oldPosition)
            internalActions.add(newPosition, action)
            adapter.notifyItemMoved(oldPosition, newPosition)
        }.attachTo(destroyer)
    }

    override fun destroy() {
        destroyer.destroy()
    }

}