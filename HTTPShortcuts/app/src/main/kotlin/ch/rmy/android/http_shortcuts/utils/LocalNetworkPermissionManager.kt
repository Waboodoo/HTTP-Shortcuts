package ch.rmy.android.http_shortcuts.utils

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import javax.inject.Inject

class LocalNetworkPermissionManager
@Inject
constructor(
    private val permissionManager: PermissionManager,
) {
    suspend fun requestLocalNetworkPermissionIfNeeded(host: String) {
        if (permissionManager.hasLocalNetworkPermission()) {
            return
        }

        val normalizedHost = host
            .let(::normalizeHost)
            .takeUnlessEmpty()
            ?: return

        if (isLocalHost(normalizedHost)) {
            // Return value intentionally ignored, as we might be wrong about needing the permission
            permissionManager.requestLocalNetworkPermissionIfNeeded()
        }
    }

    private fun normalizeHost(host: String) =
        host
            .removePrefix("[")
            .removeSuffix("]")
            .trim()
            .lowercase()

    private fun isLocalHost(host: String): Boolean {
        if (host == "localhost" || host == "local" || host.endsWith(".local")) {
            return true
        }

        // Check single-label hostname (no dots, e.g. "nas", "router")
        if (!host.contains(".") && !host.contains(":")) {
            return true
        }

        // Check IPv4
        parseIpv4(host)
            ?.let { (o1, o2, o3, o4) ->
                return when {
                    // 127.0.0.0/8 (Loopback)
                    o1 == 127 -> true
                    // 10.0.0.0/8 (RFC1918)
                    o1 == 10 -> true
                    // 172.16.0.0/12 (RFC1918)
                    o1 == 172 && o2 in 16..31 -> true
                    // 192.168.0.0/16 (RFC1918)
                    o1 == 192 && o2 == 168 -> true
                    // 169.254.0.0/16 (Link Local)
                    o1 == 169 && o2 == 254 -> true
                    // 100.64.0.0/10 (CGNAT)
                    o1 == 100 && o2 in 64..127 -> true
                    // 224.0.0.0/4 (Multicast)
                    o1 in 224..239 -> true
                    // 255.255.255.255 (Broadcast)
                    o1 == 255 && o2 == 255 && o3 == 255 && o4 == 255 -> true
                    else -> false
                }
            }

        // Check IPv6
        if (!host.contains(":")) {
            return false
        }
        if (host == "::1" || host == "0:0:0:0:0:0:0:1") {
            return true
        }
        // Link-local: fe80::/10 (starts with fe8, fe9, fea, feb)
        if (
            host.startsWith("fe8") ||
            host.startsWith("fe9") ||
            host.startsWith("fea") ||
            host.startsWith("feb")
        ) {
            return true
        }
        // Unique Local Address (ULA): fc00::/7 (starts with fc or fd)
        if (host.startsWith("fc") || host.startsWith("fd")) {
            return true
        }
        // Multicast: ff00::/8 (starts with ff)
        if (host.startsWith("ff")) {
            return true
        }

        return false
    }

    private fun parseIpv4(host: String): IntArray? {
        val parts = host.split(".", limit = 5)
            .takeIf { it.size == 4 }
            ?: return null
        return IntArray(4) { index ->
            val part = parts[index]
            if (part.startsWith("0")) {
                return null
            }
            part.toIntOrNull()
                ?.takeIf { digit -> digit in 0..255 }
                ?: return null
        }
    }
}
