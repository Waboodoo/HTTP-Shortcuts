package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.SuggestionDropdown
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.extensions.runIf

@Composable
fun FileOptions(
    allowMultiple: Boolean,
    allowStaticValues: Boolean,
    useHorizontalPadding: Boolean = true,
    fileUploadType: FileUploadType,
    sourceDirectoryName: String?,
    sourceFileName: String,
    useImageEditor: Boolean,
    staticValue: String,
    fileNameSuggestions: List<String>,
    onFileUploadTypeChanged: (FileUploadType) -> Unit,
    onSourceDirectoryNameClicked: () -> Unit,
    onSourceFileNameChanged: (String) -> Unit,
    onUseImageEditorChanged: (Boolean) -> Unit,
    onStaticValueChanged: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.TINY),
    ) {
        SelectionField(
            modifier = Modifier.runIf(useHorizontalPadding) {
                padding(horizontal = Spacing.MEDIUM)
            },
            title = stringResource(R.string.label_file_options_file_data_source),
            selectedKey = fileUploadType,
            items = buildList {
                add(FileUploadType.FILE_PICKER to stringResource(R.string.option_file_data_source_file_picker))
                if (allowMultiple) {
                    add(FileUploadType.FILE_PICKER_MULTI to stringResource(R.string.option_file_data_source_file_picker_multi))
                }
                add(FileUploadType.CAMERA to stringResource(R.string.option_file_data_source_camera))
                add(FileUploadType.FILE to stringResource(R.string.option_file_data_source_specific_file))
                if (allowStaticValues) {
                    add(FileUploadType.STATIC_VALUE to stringResource(R.string.option_file_data_source_static_value))
                }
            },
            onItemSelected = onFileUploadTypeChanged,
        )
        AnimatedVisibility(visible = fileUploadType == FileUploadType.FILE) {
            Column {
                SettingsButton(
                    title = stringResource(R.string.label_file_data_source_choose_directory),
                    subtitle = sourceDirectoryName?.let { stringResource(R.string.subtitle_file_data_source_choose_directory, it) },
                    onClick = onSourceDirectoryNameClicked,
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    var hasFocus by remember {
                        mutableStateOf(false)
                    }
                    TextField(
                        modifier = Modifier
                            .onFocusChanged {
                                hasFocus = it.isFocused
                            }
                            .runIf(useHorizontalPadding) {
                                padding(horizontal = Spacing.MEDIUM)
                            }
                            .fillMaxWidth(),
                        value = sourceFileName,
                        enabled = sourceDirectoryName != null,
                        label = {
                            Text(stringResource(R.string.label_file_data_source_file_name))
                        },
                        onValueChange = {
                            onSourceFileNameChanged(it.replace("/", ""))
                        },
                        textStyle = TextStyle(
                            fontSize = FontSize.SMALL,
                        ),
                        singleLine = true,
                    )

                    SuggestionDropdown(
                        prompt = sourceFileName,
                        isActive = hasFocus,
                        options = fileNameSuggestions.toTypedArray(),
                        minPromptLength = 1,
                        onSuggestionSelected = {
                            onSourceFileNameChanged(it)
                        },
                    )
                }
            }
        }

        AnimatedVisibility(visible = fileUploadType == FileUploadType.STATIC_VALUE) {
            TextField(
                modifier = Modifier
                    .runIf(useHorizontalPadding) {
                        padding(horizontal = Spacing.MEDIUM)
                    }
                    .fillMaxWidth(),
                value = staticValue,
                label = {
                    Text(stringResource(R.string.label_file_data_source_static_value))
                },
                onValueChange = {
                    onStaticValueChanged(it.take(20_000))
                },
                textStyle = TextStyle(
                    fontSize = FontSize.SMALL,
                ),
                maxLines = 10,
            )
        }

        AnimatedVisibility(visible = fileUploadType != FileUploadType.STATIC_VALUE) {
            Column {
                VerticalSpacer(Spacing.MEDIUM)

                Checkbox(
                    label = stringResource(R.string.label_file_upload_options_allow_image_editing),
                    checked = useImageEditor,
                    onCheckedChange = onUseImageEditorChanged,
                )
            }
        }
    }
}
