package ch.rmy.favicongrabber.models

data class ManifestIcon(
    val src: String,
    val type: String? = null,
    val sizes: String? = null,
    val purpose: String? = null,
) {
    val size: Int?
        get() = sizes
            ?.split('x')
            ?.get(0)
            ?.toIntOrNull()
}
