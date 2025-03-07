package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.core.content.getSystemService
import ch.rmy.android.framework.extensions.logInfo
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

object ServiceDiscoveryHelper {

    private const val SERVICE_NAME_SUFFIX = ".local"
    private const val SERVICE_TYPE = "_http._tcp"
    private val DISCOVER_TIMEOUT = 5.seconds

    fun isDiscoverable(uri: Uri) =
        uri.host?.endsWith(SERVICE_NAME_SUFFIX, ignoreCase = true) == true

    suspend fun discoverService(context: Context, serviceName: String): ServiceInfo =
        try {
            withTimeout(DISCOVER_TIMEOUT) {
                runDiscovery(context, serviceName)
            }
        } catch (e: TimeoutCancellationException) {
            throw ServiceLookupTimeoutException()
        }

    private suspend fun runDiscovery(context: Context, serviceName: String): ServiceInfo =
        suspendCancellableCoroutine { continuation ->
            val nsdManager = requireNotNull(context.getSystemService<NsdManager>())

            val discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                    logInfo("Start Discovery Failed")
                    continuation.resumeWithException(RuntimeException("Service Discovery Start Failed"))
                }

                override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                    continuation.resumeWithException(RuntimeException("Service Discovery Stop Failed"))
                }

                override fun onDiscoveryStarted(serviceType: String?) {
                    logInfo("Service Discovery Started")
                }

                override fun onDiscoveryStopped(serviceType: String?) {
                    logInfo("Service Discovery Stopped")
                }

                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    logInfo("Service Found: ${serviceInfo.serviceName} ${serviceInfo.serviceType}")
                    if (!isCorrectServiceType(serviceInfo)) {
                        return
                    }
                    nsdManager.resolveService(
                        serviceInfo,
                        object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                                logInfo("Resolve Failed")
                            }

                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                logInfo("Service Resolved")
                                if (serviceInfo.serviceName.contains(serviceName.removeSuffix(SERVICE_NAME_SUFFIX), ignoreCase = true)) {
                                    if (!continuation.isActive) {
                                        return
                                    }
                                    continuation.resume(
                                        ServiceInfo(
                                            address = serviceInfo.host.hostAddress!!,
                                            port = serviceInfo.port,
                                        ),
                                    )
                                }
                            }
                        },
                    )
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                    logInfo("Service Lost")
                }
            }

            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            continuation.invokeOnCancellation {
                nsdManager.stopServiceDiscovery(discoveryListener)
            }
        }

    internal fun isCorrectServiceType(serviceInfo: NsdServiceInfo) =
        normalizeServiceName(serviceInfo.serviceType) == SERVICE_TYPE

    private fun normalizeServiceName(serviceName: String) =
        serviceName.trim('.')

    class ServiceLookupTimeoutException : Exception()

    data class ServiceInfo(
        val address: String,
        val port: Int,
    )
}
