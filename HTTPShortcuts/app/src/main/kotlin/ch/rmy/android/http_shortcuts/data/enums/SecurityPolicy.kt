package ch.rmy.android.http_shortcuts.data.enums

sealed interface SecurityPolicy {
    data class FingerprintOnly(
        /**
         * Hex-encoded SHA-1 or SHA-256 fingerprint of expected (self-signed) server certificate
         */
        val certificateFingerprint: String,
    ) : SecurityPolicy {
        override fun serialize() = "$PREFIX$certificateFingerprint"

        companion object {
            const val PREFIX = "fingerprint_"
        }
    }

    data object AcceptAll : SecurityPolicy {
        override fun serialize() = "accept_all"
    }

    fun serialize(): String

    companion object {
        fun parse(value: String): SecurityPolicy? =
            when {
                value == AcceptAll.serialize() -> AcceptAll
                value.startsWith(FingerprintOnly.PREFIX) -> FingerprintOnly(value.removePrefix(FingerprintOnly.PREFIX))
                else -> null
            }
    }
}
