package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.Destroyer

open class BaseFragment : Fragment() {

    protected open val layoutResource = R.layout.empty_layout

    val destroyer = Destroyer()

    protected val args
        get() = arguments ?: Bundle()

    final override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(layoutResource, parent, false)

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        destroyer.destroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyer.destroy()
    }

    protected open fun setupViews() {

    }

}