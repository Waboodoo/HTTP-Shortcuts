package ch.rmy.android.http_shortcuts.extensions

import java.text.DateFormat
import java.text.ParseException
import java.util.Date

fun DateFormat.parseOrNull(text: String): Date? =
    try {
        parse(text)!!
    } catch (e: ParseException) {
        null
    }
