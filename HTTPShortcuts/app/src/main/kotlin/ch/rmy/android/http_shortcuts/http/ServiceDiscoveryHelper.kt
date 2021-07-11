package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import ch.rmy.android.http_shortcuts.extensions.logInfo
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

object ServiceDiscoveryHelper {

    private const val SERVICE_NAME_SUFFIX = ".local"
    private const val SERVICE_TYPE = "_http._tcp"
    private const val DISCOVER_TIMEOUT = 3000L

    fun requiresDiscovery(uri: Uri) =
        uri.host?.endsWith(SERVICE_NAME_SUFFIX, ignoreCase = true) == true

    fun discoverService(context: Context, serviceName: String): Single<ServiceInfo> = Single.create<ServiceInfo> { emitter ->
        val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                logInfo("Start Discovery Failed")
                emitter.onError(RuntimeException("Service Discovery Start Failed"))
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                emitter.onError(RuntimeException("Service Discovery Stop Failed"))
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
                nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        logInfo("Resolve Failed")
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        logInfo("Service Resolved")
                        if (serviceInfo.serviceName.contains(serviceName.removeSuffix(SERVICE_NAME_SUFFIX), ignoreCase = true)) {
                            emitter.onSuccess(ServiceInfo(
                                address = serviceInfo.host.hostAddress,
                                port = serviceInfo.port,
                            ))
                        }
                    }
                })
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                logInfo("Service Lost")
            }
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        emitter.setDisposable(object : Disposable {
            override fun dispose() {
                nsdManager.stopServiceDiscovery(discoveryListener)
            }

            override fun isDisposed() =
                emitter.isDisposed
        })
    }
        .subscribeOn(Schedulers.io())
        .ambWith(Single.error<ServiceInfo>(ServiceLookupTimeoutException()).delay(DISCOVER_TIMEOUT, TimeUnit.MILLISECONDS, true))

    private fun isCorrectServiceType(serviceInfo: NsdServiceInfo) =
        normalizeServiceName(serviceInfo.serviceType) == SERVICE_TYPE

    private fun normalizeServiceName(serviceName: String) =
        serviceName.trim('.')

    class ServiceLookupTimeoutException : Exception()

    data class ServiceInfo(
        val address: String,
        val port: Int,
    )
}