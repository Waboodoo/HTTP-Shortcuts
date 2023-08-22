package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.SettingsButton
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.extensions.runIf

@Composable
fun FileOptions(
    allowMultiple: Boolean,
    useHorizontalPadding: Boolean = true,
    fileUploadType: FileUploadType,
    fileName: String?,
    useImageEditor: Boolean,
    onFileUploadTypeChanged: (FileUploadType) -> Unit,
    onFileNameClicked: () -> Unit,
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
        var name = fileName
        DisposableEffect(fileName) {
            if (fileName != null) {
                name = fileName
            }
            onDispose { }
        }
        AnimatedVisibility(visible = fileUploadType == FileUploadType.FILE) {
            SettingsButton(
                title = stringResource(R.string.label_file_data_source_choose_file),
                subtitle = name?.let { stringResource(R.string.subtitle_file_data_source_choose_file, it) },
                onClick = onFileNameClicked,
            )
        }

        Checkbox(
            label = stringResource(R.string.label_file_upload_options_allow_image_editing),
            checked = useImageEditor,
            onCheckedChange = onUseImageEditorChanged,
        )
    }
}
