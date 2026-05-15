package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FloatingSymbolBar(
    modifier: Modifier,
    symbols: List<Char>,
    elevation: Dp = 0.dp,
    onSymbolClicked: (Char) -> Unit,
) {
    Surface(
        modifier = modifier
            .heightIn(max = Spacing.HUGE),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = elevation,
        shadowElevation = elevation,
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Spacing.SMALL),
        ) {
            symbols.forEach { symbol ->
                SymbolButton(
                    symbol = symbol,
                    onClick = {
                        onSymbolClicked(symbol)
                    },
                )
            }
        }
    }
}

@Composable
private fun SymbolButton(
    symbol: Char,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = onClick,
                role = Role.Button,
            )
            .padding(
                horizontal = Spacing.SMALL + Spacing.TINY,
                vertical = Spacing.SMALL + Spacing.TINY,
            ),
        autoSize = TextAutoSize.StepBased(
            maxFontSize = FontSize.BIG,
        ),
        text = symbol.toString(),
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@Preview
@Composable
fun FloatingSymbolBar_Preview() {
    FloatingSymbolBar(
        modifier = Modifier,
        symbols = listOf('{', '}', '[', ']', ':'),
        onSymbolClicked = {},
    )
}
