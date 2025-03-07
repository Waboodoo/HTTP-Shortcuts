package ch.rmy.android.http_shortcuts.components

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import ch.rmy.android.http_shortcuts.utils.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private val UNSUPPORTED_CHARACTERS_REGEX = "[^A-Fa-f0-9]".toRegex()

@Composable
fun ColorPickerDialog(
    initialColor: Int? = null,
    title: String? = null,
    onColorSelected: (Int) -> Unit,
    extraContent: @Composable ColumnScope.(Int) -> Unit = {},
    onDismissRequested: () -> Unit,
) {
    val viewModel = hiltViewModel<ColorPickerViewModel>()
    var color by rememberSaveable {
        mutableIntStateOf(initialColor ?: viewModel.previousIconColor ?: android.graphics.Color.BLACK)
    }
    var colorText by remember {
        mutableStateOf("")
    }
    LaunchedEffect(color) {
        colorText = color.colorIntToHexString()
    }

    val textStyle = TextStyle(
        fontSize = FontSize.SMALL,
        fontFamily = FontFamily.Monospace,
        color = Color.White,
        textAlign = TextAlign.Center,
        shadow = Shadow(
            Color.Black.copy(0.8f),
            offset = Offset(3f, 3f),
            blurRadius = 3f,
        ),
    )

    AlertDialog(
        modifier = Modifier.padding(Spacing.MEDIUM),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismissRequested,
        title = title?.let {
            {
                Text(it)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.SMALL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
            ) {
                ColorPicker(
                    color = color,
                    onColorChanged = {
                        color = it
                    },
                )

                extraContent(color)

                val containerColor = Color(color)
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        disabledContainerColor = containerColor,
                    ),
                    value = colorText,
                    onValueChange = { text ->
                        val newColor = text.replace(UNSUPPORTED_CHARACTERS_REGEX, "").uppercase().take(6)
                        colorText = newColor
                        newColor
                            .takeIf { it.length == 6 }
                            ?.hexStringToColorInt()
                            ?.let {
                                color = it
                            }
                    },
                    singleLine = true,
                    textStyle = textStyle,
                    visualTransformation = ::colorTextTransformation,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.previousIconColor = color
                    onColorSelected(color)
                },
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequested,
            ) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
}

@Stable
private fun colorTextTransformation(annotatedString: AnnotatedString): TransformedText =
    TransformedText(
        AnnotatedString("#" + annotatedString.text),
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + 1

            override fun transformedToOriginal(offset: Int): Int =
                (offset - 1).coerceAtLeast(0)
        },
    )

@HiltViewModel
class ColorPickerViewModel
@Inject
constructor(
    application: Application,
    private val settings: Settings,
) : AndroidViewModel(application) {

    var previousIconColor: Int?
        get() = settings.previousIconColor
        set(value) {
            settings.previousIconColor = value
        }
}
