package ch.rmy.android.http_shortcuts.exceptions

class InvalidUrlException(val url: String, val detail: String? = null) : UserException()