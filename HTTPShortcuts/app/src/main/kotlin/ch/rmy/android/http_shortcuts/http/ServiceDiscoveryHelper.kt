package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.core.content.getSystemService
import ch.rmy.android.framework.extensions.logInfo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlin.time.Duration.Companion.seconds

object ServiceDiscoveryHelper {

    private const val SERVICE_NAME_SUFFIX = ".local"
    private const val SERVICE_TYPE = "_http._tcp"
    private val DISCOVER_TIMEOUT = 5.seconds

    fun isDiscoverable(uri: Uri) =
        uri.host?.endsWith(SERVICE_NAME_SUFFIX, ignoreCase = true) == true

    suspend fun discoverService(context: Context, serviceName: String): ServiceInfo {
        val nsdManager = requireNotNull(context.getSystemService<NsdManager>())
        var delayJob : Job? = null
        var discoverError : Exception? = null
        var rv : ServiceInfo? = null

        logInfo("discoverService $serviceName")

        val discoveryListener = object : NsdManager.DiscoveryListener {
            private val tag = "DiscoveryListener"

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(tag, "Start Discovery Failed")
                discoverError = RuntimeException("Service Discovery Start Failed")
                delayJob?.cancel()
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                discoverError = RuntimeException("Service Discovery Stop Failed")
                delayJob?.cancel()
            }

            override fun onDiscoveryStarted(serviceType: String?) {
                Log.i(tag, "Service Discovery Started")
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.i(tag, "Service Discovery Stopped")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.i(tag, "Service Found: ${serviceInfo.serviceName} ${serviceInfo.serviceType}")
                if (!isCorrectServiceType(serviceInfo)) {
                    return
                }
                nsdManager.resolveService(
                        serviceInfo,
                        object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                                Log.i(tag, "Resolve Failed")
                            }

                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                Log.i(tag, "Service Resolved")
                                if (serviceInfo.serviceName.contains(serviceName.removeSuffix(SERVICE_NAME_SUFFIX), ignoreCase = true)) {
                                    rv = ServiceInfo(
                                        address = serviceInfo.host.hostAddress!!,
                                        port = serviceInfo.port,
                                    )
                                    delayJob?.cancel()
                                }
                            }
                        },
                )
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                Log.i(tag, "Service Lost")
            }
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        try {
            coroutineScope {
                launch {
                    delayJob = coroutineContext[Job]
                    delay(DISCOVER_TIMEOUT)
                    throw ServiceLookupTimeoutException()
                }
            }

            if(discoverError != null)
                throw discoverError!!
        } finally {
            logInfo("stop discover")
            nsdManager.stopServiceDiscovery(discoveryListener)
        }

        return rv!!
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
