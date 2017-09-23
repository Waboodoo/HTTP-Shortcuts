package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.rmy.android.http_shortcuts.R

open class BaseFragment : Fragment() {

    protected open val layoutResource = R.layout.empty_layout

    override fun onCreateView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater!!.inflate(layoutResource, parent, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    protected open fun setupViews() {

    }

}