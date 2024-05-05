package ch.rmy.android.http_shortcuts.exceptions

import kotlinx.coroutines.CancellationException

class TreatAsFailureException(override val message: String?) : CancellationException()
