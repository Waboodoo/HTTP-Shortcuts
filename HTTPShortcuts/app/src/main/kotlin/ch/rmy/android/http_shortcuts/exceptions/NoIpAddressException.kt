package ch.rmy.android.http_shortcuts.exceptions

import ch.rmy.android.http_shortcuts.data.enums.IpVersion
import java.io.IOException

class NoIpAddressException(val hostname: String, val ipVersion: IpVersion) : IOException() {
    override val message: String? =
        buildString {
            append("No ")
            append(
                when (ipVersion) {
                    IpVersion.V4 -> "IPv4"
                    IpVersion.V6 -> "IPv6"
                },
            )
            append(" address found for ")
            append(hostname)
            append(".")
        }
}
