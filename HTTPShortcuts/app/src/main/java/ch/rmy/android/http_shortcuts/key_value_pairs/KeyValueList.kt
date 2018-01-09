package ch.rmy.android.http_shortcuts.key_value_pairs

import android.content.Context
import android.support.design.widget.TextInputLayout
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
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.Destroyable
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.variables.VariableFormatter
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView

class KeyValueList<T : KeyValuePair> @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), Destroyable {

    private val button: Button by bindView(R.id.key_value_list_button)
    private val listView: ListView by bindView(R.id.key_value_list)

    private var adapter: KeyValueAdapter<T>? = null
    private var factory: ((key: String, value: String) -> T)? = null
    private var addDialogTitle: Int = 0
    private var editDialogTitle: Int = 0
    private var keyLabel: Int = 0
    private var valueLabel: Int = 0
    private var suggestionAdapter: ArrayAdapter<String>? = null

    private var controller: Controller? = null

    private val destroyer = Destroyer()

    init {
        inflate(context, R.layout.key_value_list, this)

        controller = destroyer.own(Controller())

        adapter = KeyValueAdapter(context)
        listView.adapter = adapter

        button.setOnClickListener { showAddDialog() }

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val item = adapter!!.getItem(position)
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
        val isNew = item == null

        MaterialDialog.Builder(context)
                .customView(R.layout.dialog_key_value_editor, false)
                .title(if (isNew) addDialogTitle else editDialogTitle)
                .positiveText(R.string.dialog_ok)
                .onPositive { dialog, _ ->
                    val keyField = dialog.findViewById(R.id.key_value_key) as EditText
                    val valueField = dialog.findViewById(R.id.key_value_value) as EditText
                    if (!keyField.text.toString().isEmpty()) {
                        if (isNew) {
                            val newItem = factory!!(keyField.text.toString(), valueField.text.toString())
                            adapter!!.add(newItem)
                            updateListViewHeightBasedOnChildren()
                        } else {
                            item!!.key = keyField.text.toString()
                            item.value = valueField.text.toString()
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                }
                .mapIf(!isNew) {
                    it.neutralText(R.string.dialog_remove)
                            .onNeutral { _, _ ->
                                adapter!!.remove(item)
                                updateListViewHeightBasedOnChildren()
                            }
                }
                .negativeText(R.string.dialog_cancel)
                .build()
                .also { dialog ->
                    val keyInput = dialog.findViewById(R.id.key_value_key) as AutoCompleteTextView
                    val valueInput = dialog.findViewById(R.id.key_value_value) as EditText
                    if (!isNew) {
                        keyInput.setText(item!!.key)
                        valueInput.setText(item.value)
                    }

                    val variables = controller!!.variables
                    destroyer.own(VariableFormatter.bind(keyInput, variables))
                    destroyer.own(VariableFormatter.bind(valueInput, variables))

                    (dialog.findViewById(R.id.key_value_key_layout) as TextInputLayout).hint = context.getString(keyLabel)
                    (dialog.findViewById(R.id.key_value_value_layout) as TextInputLayout).hint = context.getString(valueLabel)

                    if (suggestionAdapter != null) {
                        keyInput.setAdapter<ArrayAdapter<String>>(suggestionAdapter)
                    }
                }
                .show()
    }

    fun addItems(items: Collection<T>) {
        adapter!!.addAll(items)
        updateListViewHeightBasedOnChildren()
    }

    val items: List<T>
        get() = (0 until adapter!!.count).map { adapter!!.getItem(it) }

    fun setButtonText(resId: Int) {
        button.setText(resId)
    }

    fun setAddDialogTitle(resId: Int) {
        this.addDialogTitle = resId
    }

    fun setEditDialogTitle(resId: Int) {
        this.editDialogTitle = resId
    }

    fun setItemFactory(factory: (key: String, value: String) -> T) {
        this.factory = factory
    }

    fun setKeyLabel(resId: Int) {
        this.keyLabel = resId
    }

    fun setValueLabel(resId: Int) {
        this.valueLabel = resId
    }

    fun setSuggestions(suggestions: Array<String>) {
        this.suggestionAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, suggestions)
    }

    private fun updateListViewHeightBasedOnChildren() {
        val desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.UNSPECIFIED)
        var totalHeight = 0
        var view: View? = null
        for (i in 0 until adapter!!.count) {
            view = adapter!!.getView(i, view, listView)
            if (i == 0) {
                view.layoutParams = ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += view.measuredHeight
        }
        val params = listView.layoutParams
        params.height = totalHeight + listView.dividerHeight * (adapter!!.count - 1)
        listView.layoutParams = params
        listView.requestLayout()
    }

    override fun destroy() {
        destroyer.destroy()
    }
}
