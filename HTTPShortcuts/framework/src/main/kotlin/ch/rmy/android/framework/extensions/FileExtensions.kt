package ch.rmy.android.framework.extensions

import java.io.File
import kotlin.time.Duration

fun File.isNewerThan(duration: Duration) =
    lastModified() > System.currentTimeMillis() - duration.inWholeMilliseconds
