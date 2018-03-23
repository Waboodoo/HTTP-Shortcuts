package ch.rmy.android.http_shortcuts.key_value_pairs

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ListView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.android.http_shortcuts.utils.showSoftKeyboard
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView


class KeyValueList<T : KeyValuePair> @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val button: Button by bindView(R.id.key_value_list_button)
    private val listView: ListView by bindView(R.id.key_value_list)

    private var adapter: KeyValueAdapter<T>
    private lateinit var factory: ((key: String, value: String) -> T)
    private var suggestionAdapter: ArrayAdapter<String>? = null

    var addDialogTitle: Int = 0
    var editDialogTitle: Int = 0
    var keyLabel: Int = 0
    var valueLabel: Int = 0
    var isMultiLine: Boolean = false

    init {
        inflate(context, R.layout.key_value_list, this)

        adapter = KeyValueAdapter(context)
        listView.adapter = adapter

        button.setOnClickListener { showAddDialog() }

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val item = adapter.getItem(position)
            showEditDialog(item)
        }
    }

    private fun showAddDialog() {
        showDialog(null)
    }

    private fun showEditDialog(item: T) {
        showDialog(item)
    }

    private fun showDialog(item: T?) {
        MaterialDialog.Builder(context)
                .customView(R.layout.dialog_key_value_editor, false)
                .title(if (item == null) addDialogTitle else editDialogTitle)
                .positiveText(R.string.dialog_ok)
                .canceledOnTouchOutside(false)
                .onPositive { dialog, _ ->
                    val keyField = dialog.findViewById(R.id.key_value_key) as EditText
                    val valueField = dialog.findViewById(R.id.key_value_value) as EditText
                    if (!keyField.text.toString().isEmpty()) {
                        if (item == null) {
                            val newItem = factory.invoke(keyField.text.toString(), valueField.text.toString())
                            adapter.add(newItem)
                            updateListViewHeightBasedOnChildren()
                        } else {
                            item.key = keyField.text.toString()
                            item.value = valueField.text.toString()
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                .mapIf(item != null) {
                    it.neutralText(R.string.dialog_remove)
                            .onNeutral { _, _ ->
                                adapter.remove(item)
                                updateListViewHeightBasedOnChildren()
                            }
                }
                .negativeText(R.string.dialog_cancel)
                .build()
                .also { dialog ->
                    val keyInput = dialog.findViewById(R.id.key_value_key) as AutoCompleteTextView
                    val valueInput = dialog.findViewById(R.id.key_value_value) as EditText
                    if (item != null) {
                        keyInput.setText(item.key)
                        valueInput.setText(item.value)
                    }
                    valueInput.inputType = (if (isMultiLine) InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0) or InputType.TYPE_CLASS_TEXT
                    if (isMultiLine) {
                        valueInput.maxLines = MAX_LINES
                    }

                    (dialog.findViewById(R.id.key_value_key_layout) as TextInputLayout).hint = context.getString(keyLabel)
                    (dialog.findViewById(R.id.key_value_value_layout) as TextInputLayout).hint = context.getString(valueLabel)

                    if (suggestionAdapter != null) {
                        keyInput.setAdapter<ArrayAdapter<String>>(suggestionAdapter)
                    }

                    dialog.setOnShowListener {
                        keyInput.showSoftKeyboard()
                    }
                }
                .showIfPossible()
    }

    fun addItems(items: Collection<T>) {
        adapter.addAll(items)
        updateListViewHeightBasedOnChildren()
    }

    val items: List<T>
        get() = (0 until adapter.count).map { adapter.getItem(it) }

    fun setButtonText(resId: Int) {
        button.setText(resId)
    }

    fun setItemFactory(factory: (key: String, value: String) -> T) {
        this.factory = factory
    }

    fun setSuggestions(suggestions: Array<String>) {
        this.suggestionAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, suggestions)
    }

    private fun updateListViewHeightBasedOnChildren() {
        val desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.UNSPECIFIED)
        var totalHeight = 0
        var view: View? = null
        for (i in 0 until adapter.count) {
            view = adapter.getView(i, view, listView)
            if (i == 0) {
                view.layoutParams = ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += view.measuredHeight
        }
        val params = listView.layoutParams
        params.height = totalHeight + listView.dividerHeight * (adapter.count - 1)
        listView.layoutParams = params
        listView.requestLayout()
    }

    companion object {

        private const val MAX_LINES = 8

    }

}
