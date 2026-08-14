package ch.rmy.android.http_shortcuts.activities.globalcode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.http_shortcuts.Constants.JAVASCRIPT_SYMBOLS
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.CodeEditorField
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.FloatingSymbolBar
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.insertAtCursor
import ch.rmy.android.http_shortcuts.utils.SyntaxHighlighter

@Composable
fun GlobalScriptingContent(
    globalCode: String,
    onGlobalCodeChanged: (String) -> Unit,
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = globalCode,
                selection = TextRange(globalCode.length),
            ),
        )
    }

    SideEffect(globalCode) {
        if (globalCode != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = globalCode,
            )
        }
    }

    EventHandler { event ->
        when (event) {
            is GlobalScriptingEvent.InsertCodeSnippet -> consume {
                textFieldValue = textFieldValue.insertAtCursor(event.textBeforeCursor, event.textAfterCursor)
                onGlobalCodeChanged(textFieldValue.text)
            }
            else -> false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        CodeEditorField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.SMALL)
                .weight(1f),
            language = SyntaxHighlighter.Languages.JS,
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onGlobalCodeChanged(it.text)
            },
            placeholder = stringResource(R.string.placeholder_javascript_global_scripting),
        )

        val windowInfo = LocalWindowInfo.current
        if (windowInfo.containerSize.height > 1400) {
            FloatingSymbolBar(
                modifier = Modifier
                    .padding(bottom = Spacing.MEDIUM)
                    .padding(horizontal = Spacing.MEDIUM)
                    .padding(end = Spacing.HUGE + Spacing.SMALL),
                symbols = JAVASCRIPT_SYMBOLS,
                elevation = 4.dp,
                onSymbolClicked = { symbol ->
                    textFieldValue = textFieldValue.insertAtCursor(before = symbol.toString())
                    onGlobalCodeChanged(textFieldValue.text)
                },
            )
        }
    }
}
