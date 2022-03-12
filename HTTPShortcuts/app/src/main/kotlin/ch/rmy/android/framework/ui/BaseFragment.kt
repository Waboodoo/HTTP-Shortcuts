package ch.rmy.android.framework.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.showIfPossible
import ch.rmy.android.framework.utils.Destroyer
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState

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

    private var currentDialogState: DialogState? = null
    private var currentDialog: Dialog? = null
    private var savedDialogState: Bundle? = null

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

    protected open fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ViewModelEvent.OpenActivity -> {
                logInfo("handleEvent: Opening activity for ${event.intentBuilder}")
                event.intentBuilder.startActivity(this, event.requestCode)
            }
            else -> (requireActivity() as BaseActivity).handleEvent(event)
        }
    }

    fun setDialogState(dialogState: DialogState?, viewModel: WithDialog) {
        if (currentDialogState == dialogState) {
            return
        }
        currentDialog?.dismiss()
        currentDialogState = dialogState
        currentDialog = dialogState?.createDialog(requireContext(), viewModel)
        currentDialog?.showIfPossible()
        currentDialog?.let { dialog ->
            savedDialogState?.let { dialogState ->
                currentDialogState?.restoreInstanceState(dialog, dialogState)
            }
        }
        savedDialogState = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentDialog?.let { dialog ->
            currentDialogState?.let { dialogState ->
                outState.putBundle(EXTRA_DIALOG_INSTANCE_STATE, dialogState.saveInstanceState(dialog))
            }
        }
    }

    companion object {
        private const val EXTRA_DIALOG_INSTANCE_STATE = "dialog-instance-state"
    }
}
