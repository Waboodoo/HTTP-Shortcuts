package ch.rmy.android.http_shortcuts.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.visible
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class LabelledSpinner @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : com.satsuware.usefulviews.LabelledSpinner(context, attrs, defStyleAttr) {

    private val selectionChangeSubject = PublishSubject.create<String>()

    val selectionChanges: Observable<String>
        get() = selectionChangeSubject

    var items: List<Item> = emptyList()
        set(value) {
            field = value
            setItemsArray(value.map { it.value ?: it.key })
        }

    fun setItemsFromPairs(items: List<Pair<String, String>>) {
        this.items = items.map { (key, value) -> Item(key, value) }
    }

    init {
        val paddingTop = context.resources.getDimensionPixelSize(R.dimen.spinner_padding_top)
        label.setPadding(0, paddingTop, 0, 0)
        errorLabel.visible = false

        onItemChosenListener = object : OnItemChosenListener {
            override fun onItemChosen(labelledSpinner: View?, adapterView: AdapterView<*>?, itemView: View?, position: Int, id: Long) {
                selectedItem = items[position].key
            }

            override fun onNothingChosen(labelledSpinner: View?, adapterView: AdapterView<*>?) {

            }
        }
    }

    var selectedItem: String = ""
        set(value) {
            items
            val index = items
                .indexOfFirst { it.key == value }
                .takeUnless { it == -1 }
                ?: return
            val before = field
            field = value
            setSelection(index)
            if (before != value && before.isNotEmpty()) {
                selectionChangeSubject.onNext(value)
            }
        }

    data class Item(val key: String, val value: String? = null)

}