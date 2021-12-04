package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import ch.rmy.android.http_shortcuts.utils.Destroyer

abstract class BaseFragment<Binding : ViewBinding> : Fragment() {

    val destroyer = Destroyer()

    protected val args
        get() = arguments ?: Bundle()

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private var _binding: Binding? = null
    protected val binding: Binding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = getBinding(inflater, container)
        return binding.root
    }

    abstract fun getBinding(inflater: LayoutInflater, container: ViewGroup?): Binding

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        destroyer.destroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyer.destroy()
    }

    protected open fun setupViews() {
    }

    open fun onBackPressed(): Boolean = false
}
