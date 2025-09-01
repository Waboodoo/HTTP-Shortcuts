package ch.rmy.android.http_shortcuts.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalContext
import ch.rmy.android.framework.utils.localization.Localizable

@Stable
@Composable
fun Localizable.localize(): String =
    localize(LocalContext.current).toString()
