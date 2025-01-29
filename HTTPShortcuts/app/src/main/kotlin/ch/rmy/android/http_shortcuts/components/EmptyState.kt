package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun EmptyState(title: String? = null, description: String? = null) {
    Column(
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .padding(Spacing.MEDIUM)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(
            space = Spacing.MEDIUM,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        if (title != null) {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontSize = FontSize.BIG,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
        if (description != null) {
            Text(
                text = description,
                textAlign = TextAlign.Center,
                fontSize = FontSize.MEDIUM,
                lineHeight = FontSize.BIG,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
    }
}
