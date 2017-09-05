package ch.rmy.android.http_shortcuts.utils

import android.view.View
import android.widget.AdapterView

import com.satsuware.usefulviews.LabelledSpinner

abstract class OnItemChosenListener : LabelledSpinner.OnItemChosenListener {

    override fun onItemChosen(labelledSpinner: View?, adapterView: AdapterView<*>?, itemView: View?, position: Int, id: Long) {
        onSelectionChanged()
    }

    abstract fun onSelectionChanged()

    override fun onNothingChosen(labelledSpinner: View?, adapterView: AdapterView<*>?) {

    }

}
