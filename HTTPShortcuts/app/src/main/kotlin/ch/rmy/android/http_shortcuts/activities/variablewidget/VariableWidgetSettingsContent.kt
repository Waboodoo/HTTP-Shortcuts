package ch.rmy.android.http_shortcuts.activities.variablewidget

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variablewidget.models.SelectableShortcut
import ch.rmy.android.http_shortcuts.activities.variablewidget.models.SelectableVariable
import ch.rmy.android.http_shortcuts.components.ColorPickerDialog
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.enums.WidgetBackgroundType

private const val BACKGROUND_TYPE_COLOR = "color"

@Composable
fun VariableWidgetSettingsContent(
    variables: List<SelectableVariable>,
    selectedVariable: SelectableVariable?,
    variableValue: String?,
    fontSize: Int,
    title: String,
    background: WidgetBackgroundType?,
    shortcutId: ShortcutId?,
    shortcuts: List<SelectableShortcut>,
    onVariableSelected: (GlobalVariableId?) -> Unit,
    onFontSizeChanged: (Int) -> Unit,
    onTitleChanged: (String) -> Unit,
    onBackgroundChanged: (WidgetBackgroundType?) -> Unit,
    onShortcutSelected: (ShortcutId?) -> Unit,
) {
    var colorPickerInitialColor by remember {
        mutableStateOf<Int?>(null)
    }
    colorPickerInitialColor?.let { initialColor ->
        ColorPickerDialog(
            initialColor = initialColor,
            withAlpha = true,
            onColorSelected = { color ->
                onBackgroundChanged(
                    WidgetBackgroundType.Color(color),
                )
                colorPickerInitialColor = null
            },
            onDismissRequested = {
                colorPickerInitialColor = null
            },
        )
    }

    if (variables.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.empty_state_variable_widget, stringResource(R.string.variable_type_constant)),
                textAlign = TextAlign.Center,
                fontSize = FontSize.MEDIUM,
                lineHeight = FontSize.BIG,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MEDIUM),
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 150.dp)
                .padding(
                    horizontal = Spacing.HUGE,
                    vertical = Spacing.BIG,
                ),
            contentAlignment = Alignment.Center,
        ) {
            WidgetPreview(
                value = variableValue,
                fontSize = fontSize,
                title = title,
                background = background,
            )
        }

        VerticalSpacer(Spacing.MEDIUM)

        HorizontalDivider()

        VerticalSpacer(Spacing.MEDIUM)

        SelectionField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.MEDIUM),
            title = stringResource(R.string.label_variable_widget_select_variable),
            selectedKey = selectedVariable?.variableId,
            items = buildList {
                if (variables.none { it.variableId == selectedVariable?.variableId }) {
                    add(null to "---")
                }
                variables.forEach { variable ->
                    add(variable.variableId to variable.variableKey)
                }
            },
            onItemSelected = onVariableSelected,
        )

        FontSizeSelection(
            fontSize = fontSize,
            onFontSizeChanged = onFontSizeChanged,
        )

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.MEDIUM),
            label = {
                Text(stringResource(R.string.label_variable_widget_title))
            },
            value = title,
            onValueChange = onTitleChanged,
            maxLines = 2,
        )

        VerticalSpacer(Spacing.SMALL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.MEDIUM),
            ) {
                BackgroundTypeSelection(
                    backgroundType = when (background) {
                        is WidgetBackgroundType.Color -> BACKGROUND_TYPE_COLOR
                        else -> null
                    },
                    onBackgroundTypeSelected = { backgroundType ->
                        onBackgroundChanged(
                            if (backgroundType == BACKGROUND_TYPE_COLOR) {
                                WidgetBackgroundType.Color(
                                    color = (background as? WidgetBackgroundType.Color)?.color ?: Color.White.toArgb(),
                                )
                            } else {
                                null
                            },
                        )
                    },
                )
                AnimatedVisibility(visible = background is WidgetBackgroundType.Color) {
                    Box(modifier = Modifier.padding(top = Spacing.SMALL)) {
                        (background as? WidgetBackgroundType.Color)?.let { background ->
                            BackgroundColorButton(
                                background.color,
                                background.getHexString(),
                                onColorButtonClicked = {
                                    colorPickerInitialColor = Color(background.color).copy(alpha = 1f).toArgb()
                                },
                            )
                        }
                    }
                }
            }

            VerticalSpacer(Spacing.SMALL)
        }

        SelectionField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.MEDIUM),
            title = stringResource(R.string.label_variable_widget_select_shortcut),
            selectedKey = shortcutId,
            items = buildList {
                add(null to "---")
                shortcuts.forEach { shortcut ->
                    add(shortcut.shortcutId to shortcut.name)
                }
            },
            onItemSelected = onShortcutSelected,
        )

        VerticalSpacer(Spacing.MEDIUM)
    }
}

@Composable
private fun WidgetPreview(
    value: String?,
    fontSize: Int,
    title: String,
    background: WidgetBackgroundType?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when (background) {
                    is WidgetBackgroundType.Color -> Color(background.color)
                    null -> colorResource(R.color.variable_widget_background)
                },
                shape = RoundedCornerShape(8.dp),
            )
            .padding(
                vertical = Spacing.SMALL,
                horizontal = Spacing.MEDIUM,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                maxLines = 2,
                fontSize = fontSize.sp * 0.75,
                lineHeight = (fontSize * 1.2 * 0.75).sp,
                color = colorResource(R.color.variable_widget_foreground),
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            VerticalSpacer(Spacing.SMALL)
        }
        Text(
            text = value?.ifEmpty { "-" } ?: "???",
            maxLines = 10,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.2).sp,
            color = colorResource(R.color.variable_widget_foreground),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FontSizeSelection(
    fontSize: Int,
    onFontSizeChanged: (Int) -> Unit,
) {
    SelectionField(
        modifier = Modifier
            .padding(vertical = Spacing.SMALL)
            .padding(horizontal = Spacing.MEDIUM),
        title = stringResource(R.string.label_font_size),
        selectedKey = fontSize,
        items = FONT_SIZES.toItems(),
        itemFactory = { key, value ->
            Text(
                text = value,
                fontSize = key.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        onItemSelected = onFontSizeChanged,
    )
}

@Composable
private fun BackgroundTypeSelection(
    backgroundType: String?,
    onBackgroundTypeSelected: (String?) -> Unit,
) {
    SelectionField(
        title = stringResource(R.string.label_category_background),
        selectedKey = backgroundType,
        items = listOf(
            null to stringResource(R.string.category_background_type_default),
            "color" to stringResource(R.string.category_background_type_color),
        ),
        onItemSelected = onBackgroundTypeSelected,
    )
}

@Composable
private fun BackgroundColorButton(
    backgroundColor: Int,
    backgroundColorAsText: String,
    onColorButtonClicked: () -> Unit,
) {
    val textStyle = TextStyle(
        fontSize = FontSize.MEDIUM,
        fontFamily = FontFamily.Monospace,
        color = Color.White,
        shadow = Shadow(
            Color.Black.copy(0.8f),
            offset = Offset(3f, 3f),
            blurRadius = 3f,
        ),
    )

    Text(
        text = backgroundColorAsText,
        style = textStyle,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(backgroundColor))
            .clickable {
                onColorButtonClicked()
            }
            .padding(Spacing.SMALL),
    )
}

private val FONT_SIZES = listOf(
    8 to R.string.font_size_tiny,
    12 to R.string.font_size_very_small,
    17 to R.string.font_size_small,
    22 to R.string.font_size_regular,
    28 to R.string.font_size_large,
    36 to R.string.font_size_very_large,
    42 to R.string.font_size_huge,
)

@Composable
private fun <T> List<Pair<T, Int>>.toItems() =
    map { (value, label) -> value to stringResource(label) }

@Preview
@Composable
private fun WidgetPreview_Preview() {
    WidgetPreview(
        value = "Hello World",
        fontSize = 20,
        title = "",
        background = null,
    )
}

@Preview
@Composable
private fun WidgetPreview_WithTitle_Preview() {
    WidgetPreview(
        value = "Hello World",
        fontSize = 20,
        title = "My Widget",
        background = null,
    )
}

@Preview
@Composable
private fun WidgetPreview_WithTitleAndBackground_Preview() {
    WidgetPreview(
        value = "Hello World",
        fontSize = 20,
        title = "My Widget",
        background = WidgetBackgroundType.Color(Color.White.toArgb()),
    )
}
