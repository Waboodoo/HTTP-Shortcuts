package ch.rmy.android.http_shortcuts.variables.types

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.VariableEditorActivity
import ch.rmy.android.http_shortcuts.realm.models.Variable

open class VariableEditorFragment : Fragment() {

    protected open val layoutResource = R.layout.empty_layout

    override fun onCreateView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(layoutResource, parent, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view!!)
    }

    protected open fun setupViews(parent: View) {

    }

    override fun onStart() {
        super.onStart()
        (activity as VariableEditorActivity).onFragmentStarted()
    }

    open fun updateViews(variable: Variable) {

    }

    open fun compileIntoVariable(variable: Variable) {

    }

    open fun validate() = true

}
