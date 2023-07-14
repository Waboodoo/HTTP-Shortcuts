package ch.rmy.android.http_shortcuts.extensions

private val CERTIFICATE_FINGERPRINT_REGEX = "([0-9A-Fa-f]{40}|[0-9A-Fa-f]{64})".toRegex()

fun String.isValidCertificateFingerprint() =
    matches(CERTIFICATE_FINGERPRINT_REGEX)
