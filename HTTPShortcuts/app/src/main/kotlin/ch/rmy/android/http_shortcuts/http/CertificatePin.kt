package ch.rmy.android.http_shortcuts.http

data class CertificatePin(
    val pattern: String,
    val hash: ByteArray,
) {
    val isSha256: Boolean
        get() = hash.size == 32
}
