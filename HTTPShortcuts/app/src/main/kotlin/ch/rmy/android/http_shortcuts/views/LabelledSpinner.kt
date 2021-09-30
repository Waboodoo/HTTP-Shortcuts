package ch.rmy.android.http_shortcuts.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.setContentView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView

class LabelledSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    LinearLayoutCompat(context, attrs, defStyleAttr) {

    private val label: TextView by bindView(R.id.label)
    private val spinner: Spinner by bindView(R.id.spinner)

    private val selectionChangeSubject = PublishSubject.create<String>()

    val selectionChanges: Observable<String>
        get() = selectionChangeSubject

    var items: List<Item> = emptyList()
        set(value) {
            field = value
            spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, value.map { it.value ?: it.key })
        }

    fun setItemsFromPairs(items: List<Pair<String, String>>) {
        this.items = items.map { (key, value) -> Item(key, value) }
    }

    init {
        orientation = VERTICAL
        setContentView(R.layout.labelled_spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                selectedItem = items[position].key
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        if (attrs != null) {
            var a: TypedArray? = null
            try {
                @SuppressLint("Recycle")
                a = context.obtainStyledAttributes(attrs, ATTRIBUTE_IDS)
                label.text = a.getText(ATTRIBUTE_IDS.indexOf(android.R.attr.text)) ?: ""
            } finally {
                a?.recycle()
            }
        }
    }

    var selectedItem: String = ""
        set(value) {
            val index = items
                .indexOfFirst { it.key == value }
                .takeUnless { it == -1 }
                ?: return
            val before = field
            field = value
            spinner.setSelection(index)
            if (before != value && before.isNotEmpty()) {
                selectionChangeSubject.onNext(value)
            }
        }

    data class Item(val key: String, val value: String? = null)

    companion object {

        private val ATTRIBUTE_IDS = intArrayOf(android.R.attr.text)

    }

}