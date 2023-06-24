package ch.rmy.android.http_shortcuts.exceptions

import kotlinx.coroutines.CancellationException

class UserAbortException(val abortAll: Boolean) : CancellationException()
