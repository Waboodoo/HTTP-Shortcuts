package ch.rmy.android.http_shortcuts.activities.categories.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import ch.rmy.android.http_shortcuts.Constants
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.editor.models.CategoryBackground
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.data.enums.CategoryAlignment
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior

@Composable
fun CategoryEditorContent(
    colorButtonVisible: Boolean,
    categoryName: String,
    categoryLayoutType: CategoryLayoutType,
    categoryAlignment: CategoryAlignment,
    categoryBackgroundType: CategoryBackground,
    backgroundColor: Int,
    backgroundColorAsText: String,
    selectedClickActionOption: ShortcutClickBehavior?,
    scale: Float,
    hiddenLabels: Boolean,
    onCategoryNameChanged: (String) -> Unit,
    onLayoutTypeSelected: (CategoryLayoutType) -> Unit,
    onAlignmentChanged: (CategoryAlignment) -> Unit,
    onBackgroundTypeSelected: (CategoryBackground) -> Unit,
    onColorButtonClicked: () -> Unit,
    onClickActionOptionSelected: (ShortcutClickBehavior?) -> Unit,
    onScaleChanged: (Float) -> Unit,
    onHiddenLabelsChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(Spacing.MEDIUM)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        CategoryName(
            name = categoryName,
            onNameChanged = onCategoryNameChanged,
        )

        CategoryLayoutTypeSelection(
            categoryLayoutType = categoryLayoutType,
            onLayoutTypeSelected = onLayoutTypeSelected,
        )

        CategoryAlignmentSelection(
            categoryAlignment = categoryAlignment,
            onAlignmentSelected = onAlignmentChanged,
        )

        CategoryHiddenLabelsSelection(
            hiddenLabels = hiddenLabels,
            enabled = categoryLayoutType != CategoryLayoutType.LINEAR_LIST,
            onHiddenLabelsChanged = onHiddenLabelsChanged,
        )

        CategoryScaleSelection(
            scale = scale,
            onScaleChanged = onScaleChanged,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            CategoryBackgroundTypeSelection(
                categoryBackgroundType = categoryBackgroundType,
                onBackgroundTypeSelected = onBackgroundTypeSelected,
            )
            AnimatedVisibility(visible = colorButtonVisible) {
                Box(modifier = Modifier.padding(top = Spacing.SMALL)) {
                    BackgroundColorButton(
                        backgroundColor = backgroundColor,
                        backgroundColorAsText = backgroundColorAsText,
                        onColorButtonClicked = onColorButtonClicked,
                    )
                }
            }
        }

        ClickActionSelection(
            selectedClickActionOption = selectedClickActionOption,
            onClickActionOptionSelected = onClickActionOptionSelected,
        )
    }
}

@Composable
private fun CategoryName(name: String, onNameChanged: (String) -> Unit) {
    TextField(
        modifier = Modifier
            .fillMaxWidth(),
        label = {
            Text(stringResource(R.string.label_category_name))
        },
        placeholder = {
            Text(stringResource(R.string.placeholder_category_name))
        },
        value = name,
        onValueChange = {
            onNameChanged(it.take(Constants.CATEGORY_NAME_MAX_LENGTH))
        },
        singleLine = true,
    )
}

@Composable
private fun CategoryLayoutTypeSelection(
    categoryLayoutType: CategoryLayoutType,
    onLayoutTypeSelected: (CategoryLayoutType) -> Unit,
) {
    SelectionField(
        title = stringResource(R.string.label_category_layout_type),
        selectedKey = categoryLayoutType,
        items = listOf(
            CategoryLayoutType.LINEAR_LIST to stringResource(R.string.layout_type_linear_list),
            CategoryLayoutType.DENSE_GRID to stringResource(R.string.layout_type_dense_grid),
            CategoryLayoutType.MEDIUM_GRID to stringResource(R.string.layout_type_medium_grid),
            CategoryLayoutType.WIDE_GRID to stringResource(R.string.layout_type_wide_grid),
        ),
        onItemSelected = onLayoutTypeSelected,
    )
}

@Composable
private fun CategoryAlignmentSelection(
    categoryAlignment: CategoryAlignment,
    onAlignmentSelected: (CategoryAlignment) -> Unit,
) {
    SelectionField(
        title = stringResource(R.string.label_category_vertical_alignment),
        selectedKey = categoryAlignment,
        items = listOf(
            CategoryAlignment.TOP to stringResource(R.string.category_vertical_alignment_top),
            CategoryAlignment.CENTER to stringResource(R.string.category_vertical_alignment_center),
            CategoryAlignment.BOTTOM to stringResource(R.string.category_vertical_alignment_bottom),
        ),
        onItemSelected = onAlignmentSelected,
    )
}

@Composable
private fun CategoryScaleSelection(
    scale: Float,
    onScaleChanged: (Float) -> Unit,
) {
    SelectionField(
        title = stringResource(R.string.label_category_scale),
        selectedKey = scale,
        items = listOf(
            0.5f to "0.5x",
            0.75f to "0.75x",
            1f to "1x",
            1.25f to "1.25x",
            1.5f to "1.5x",
            2f to "2x",
            2.5f to "2.5x",
            3f to "3x",
            3.5f to "3.5x",
            4f to "4x",
        ),
        onItemSelected = onScaleChanged,
    )
}

@Composable
private fun CategoryHiddenLabelsSelection(
    hiddenLabels: Boolean,
    enabled: Boolean,
    onHiddenLabelsChanged: (Boolean) -> Unit,
) {
    SelectionField(
        title = stringResource(R.string.label_category_shortcut_label_visibility),
        selectedKey = hiddenLabels,
        items = listOf(
            false to stringResource(R.string.option_shortcut_labels_visible),
            true to stringResource(R.string.option_shortcut_labels_hidden),
        ),
        enabled = enabled,
        onItemSelected = onHiddenLabelsChanged,
    )
}

@Composable
private fun CategoryBackgroundTypeSelection(
    categoryBackgroundType: CategoryBackground,
    onBackgroundTypeSelected: (CategoryBackground) -> Unit,
) {
    SelectionField(
        title = stringResource(R.string.label_category_background),
        selectedKey = categoryBackgroundType,
        items = listOf(
            CategoryBackground.DEFAULT to stringResource(R.string.category_background_type_default),
            CategoryBackground.COLOR to stringResource(R.string.category_background_type_color),
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

@Composable
private fun ClickActionSelection(
    selectedClickActionOption: ShortcutClickBehavior?,
    onClickActionOptionSelected: (ShortcutClickBehavior?) -> Unit,
) {
    SelectionField(
        title = stringResource(R.string.settings_click_behavior),
        selectedKey = selectedClickActionOption,
        items = listOf(
            null to stringResource(R.string.settings_click_behavior_global_default),
            ShortcutClickBehavior.RUN to stringResource(R.string.settings_click_behavior_run),
            ShortcutClickBehavior.EDIT to stringResource(R.string.settings_click_behavior_edit),
            ShortcutClickBehavior.MENU to stringResource(R.string.settings_click_behavior_menu),
        ),
        onItemSelected = onClickActionOptionSelected,
    )
}
