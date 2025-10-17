package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun clickOnlyInteractionSource(onClick: () -> Unit) = remember(onClick) { MutableInteractionSource() }
    .also { interactionSource ->
        LaunchedEffect(onClick, interactionSource) {
            interactionSource.interactions.collect {
                if (it is PressInteraction.Release) {
                    onClick()
                }
            }
        }
    }
