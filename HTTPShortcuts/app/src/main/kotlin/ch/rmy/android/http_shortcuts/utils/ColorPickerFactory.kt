package ch.rmy.android.http_shortcuts.utils

import android.app.Dialog
import android.graphics.Color
import android.text.InputFilter
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.core.widget.addTextChangedListener
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.databinding.DialogColorPickerBinding
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import com.skydoves.colorpickerview.listeners.ColorListener
import javax.inject.Inject

class ColorPickerFactory
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) {

    fun createColorPicker(
        onColorPicked: (Int) -> Unit,
        onDismissed: () -> Unit,
        title: Localizable = Localizable.EMPTY,
        @ColorInt initialColor: Int? = null,
    ): Dialog {
        val activity = activityProvider.getActivity()
        val binding = DialogColorPickerBinding.inflate(LayoutInflater.from(activity))

        binding.colorPickerView.attachBrightnessSlider(binding.brightnessSlideBar)
        var suppressTextWatcher = false
        var selectedColor = initialColor ?: Color.WHITE
        binding.colorPickerView.setInitialColor(selectedColor)

        fun updateColorInputView() {
            suppressTextWatcher = true
            val selectionStart = binding.inputColor.selectionStart
            val selectionEnd = binding.inputColor.selectionEnd
            binding.inputColor.setText(selectedColor.colorIntToHexString())
            suppressTextWatcher = false
            if (selectionStart != -1 && selectionEnd != -1) {
                binding.inputColor.setSelection(selectionStart, selectionEnd)
            } else {
                binding.inputColor.setSelection(6)
            }
            binding.inputColorBackdrop.setBackgroundColor(selectedColor)
        }

        binding.inputColor.filters = arrayOf(
            InputFilter { source, _, _, _, _, _ -> source.replace("[^A-Fa-f0-9]".toRegex(), "").uppercase() },
            InputFilter.LengthFilter(6),
        )
        binding.inputColor.addTextChangedListener { text ->
            binding.inputColorBackdrop.text = "#$text "
            if (!suppressTextWatcher) {
                val color = text?.toString()
                    ?.takeIf { it.length == 6 }
                    ?.hexStringToColorInt()
                if (color != null) {
                    selectedColor = color
                    binding.colorPickerView.selectByHsvColor(color)
                    updateColorInputView()
                }
            }
        }

        binding.colorPickerView.setColorListener(object : ColorListener {
            override fun onColorSelected(color: Int, fromUser: Boolean) {
                selectedColor = color
                updateColorInputView()
            }
        })
        updateColorInputView()

        return DialogBuilder(activity)
            .runIfNotNull(title) {
                title(it)
            }
            .view(binding.root)
            .positive(R.string.dialog_ok) {
                onColorPicked(selectedColor)
            }
            .negative(R.string.dialog_cancel)
            .dismissListener {
                onDismissed()
            }
            .build()
    }
}
