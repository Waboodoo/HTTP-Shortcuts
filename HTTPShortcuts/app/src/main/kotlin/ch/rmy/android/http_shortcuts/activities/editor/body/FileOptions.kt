package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.extensions.runIf

@Composable
fun FileOptions(
    allowMultiple: Boolean,
    useHorizontalPadding: Boolean = true,
    fileUploadType: FileUploadType,
    sourceDirectoryName: String?,
    sourceFileName: String,
    useImageEditor: Boolean,
    onFileUploadTypeChanged: (FileUploadType) -> Unit,
    onSourceDirectoryNameClicked: () -> Unit,
    onSourceFileNameChanged: (String) -> Unit,
    onUseImageEditorChanged: (Boolean) -> Unit,
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

                TextField(
                    modifier = Modifier
                        .runIf(useHorizontalPadding) {
                            padding(horizontal = Spacing.MEDIUM)
                        }
                        .fillMaxWidth(),
                    value = sourceFileName,
                    enabled = sourceDirectoryName != null,
                    label = {
                        Text(stringResource(R.string.label_file_data_source_file_name))
                    },
                    onValueChange = onSourceFileNameChanged,
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                    ),
                    singleLine = true,
                )
            }
        }

        VerticalSpacer(Spacing.MEDIUM)

        Checkbox(
            label = stringResource(R.string.label_file_upload_options_allow_image_editing),
            checked = useImageEditor,
            onCheckedChange = onUseImageEditorChanged,
        )
    }
}
