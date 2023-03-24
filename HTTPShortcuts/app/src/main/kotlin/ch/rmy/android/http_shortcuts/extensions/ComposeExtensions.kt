package ch.rmy.android.http_shortcuts.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalContext
import ch.rmy.android.framework.utils.localization.Localizable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val shortTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@Stable
@Composable
fun Localizable.localize(): String =
    localize(LocalContext.current).toString()

@Stable
fun LocalDateTime.formatShortTime(): String =
    shortTimeFormatter.format(this)
