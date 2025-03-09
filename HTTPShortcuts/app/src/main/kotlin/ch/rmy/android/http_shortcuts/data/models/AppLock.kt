package ch.rmy.android.http_shortcuts.data.models

data class AppLock(
    val passwordHash: String,
    val useBiometrics: Boolean,
)
