package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import ch.rmy.android.http_shortcuts.R

@Composable
fun TextContainer(
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .background(
                colorResource(R.color.textarea_background),
                shape = RoundedCornerShape(Spacing.TINY),
            )
            .padding(Spacing.SMALL)
    ) {
        content()
    }
}
