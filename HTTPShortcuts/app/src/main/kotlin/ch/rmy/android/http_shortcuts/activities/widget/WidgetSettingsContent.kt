package ch.rmy.android.http_shortcuts.activities.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.OrderedOptionsSlider
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.ShortcutIcon
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon as ShortcutIconModel

private val ICON_SCALE_OPTIONS = arrayOf(0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f)

@Composable
fun WidgetSettingsContent(
    showLabel: Boolean,
    showIcon: Boolean,
    labelColor: Color,
    labelColorText: String,
    iconScale: Float,
    shortcutName: String,
    shortcutIcon: ShortcutIconModel,
    onShowLabelChanged: (Boolean) -> Unit,
    onShowIconChanged: (Boolean) -> Unit,
    onLabelColorButtonClicked: () -> Unit,
    onIconScaleChanged: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.SMALL)
                .sizeIn(minHeight = 120.dp),
            contentAlignment = Alignment.Center,
        ) {
            WidgetPreview(
                showLabel = showLabel,
                showIcon = showIcon,
                labelColor = labelColor,
                iconScale = iconScale,
                shortcutName = shortcutName,
                shortcutIcon = shortcutIcon,
            )
        }

        HorizontalDivider()

        Checkbox(
            checked = showIcon,
            enabled = showLabel,
            label = stringResource(R.string.label_show_widget_icon),
            onCheckedChange = onShowIconChanged,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            modifier = Modifier.padding(Spacing.MEDIUM),
        ) {
            Text(
                text = stringResource(R.string.label_widget_icon_size),
            )
            OrderedOptionsSlider(
                modifier = Modifier
                    .fillMaxWidth(),
                options = ICON_SCALE_OPTIONS,
                enabled = showIcon,
                value = iconScale,
                onValueChange = {
                    onIconScaleChanged(it)
                },
            )

            VerticalSpacer(Spacing.TINY)
        }

        HorizontalDivider()

        Checkbox(
            checked = showLabel,
            enabled = showIcon,
            label = stringResource(R.string.label_show_widget_label),
            onCheckedChange = onShowLabelChanged,
        )

        SettingsButton(
            enabled = showLabel,
            title = stringResource(R.string.label_widget_label_color),
            subtitle = labelColorText,
            onClick = onLabelColorButtonClicked,
        )

        HorizontalDivider()
    }
}

@Composable
private fun WidgetPreview(
    showLabel: Boolean,
    showIcon: Boolean,
    labelColor: Color,
    iconScale: Float = 1f,
    shortcutName: String,
    shortcutIcon: ShortcutIconModel,
) {
    Column(
        modifier = Modifier
            .background(colorResource(R.color.widget_preview_background), RoundedCornerShape(Spacing.TINY))
            .sizeIn(minWidth = 100.dp, minHeight = 120.dp)
            .padding(Spacing.SMALL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL, Alignment.CenterVertically),
    ) {
        if (showIcon) {
            ShortcutIcon(
                shortcutIcon,
                size = 44.dp * iconScale,
            )
            VerticalSpacer(22.dp * (1f - iconScale))
        }

        if (showLabel) {
            Text(
                shortcutName,
                color = labelColor,
                minLines = if (showIcon) 2 else 1,
                maxLines = if (showIcon) 2 else 4,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun WidgetPreview_Preview1() {
    WidgetPreview(
        showLabel = true,
        showIcon = true,
        labelColor = Color.White,
        shortcutName = "Shortcut",
        shortcutIcon = ShortcutIconModel.NoIcon,
    )
}

@Preview
@Composable
private fun WidgetPreview_Preview2() {
    WidgetPreview(
        showLabel = true,
        showIcon = true,
        iconScale = 0.5f,
        labelColor = Color.Red,
        shortcutName = "Shortcut with a rather long text",
        shortcutIcon = ShortcutIconModel.NoIcon,
    )
}

@Preview
@Composable
private fun WidgetPreview_Preview3() {
    WidgetPreview(
        showLabel = false,
        showIcon = true,
        labelColor = Color.White,
        shortcutName = "Shortcut",
        shortcutIcon = ShortcutIconModel.NoIcon,
    )
}

@Preview
@Composable
private fun WidgetPreview_Preview4() {
    WidgetPreview(
        showLabel = true,
        showIcon = false,
        labelColor = Color.White,
        shortcutName = "Shortcut",
        shortcutIcon = ShortcutIconModel.NoIcon,
    )
}
