package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.fragment.app.FragmentActivity
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.PlayServicesUtil
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import org.json.JSONObject
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class GetLocationAction : BaseAction() {

    @Inject
    lateinit var playServicesUtil: PlayServicesUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        checkForPlayServices()
            .andThen(requestLocationPermissionIfNeeded(executionContext.context as FragmentActivity))
            .andThen(fetchLocation(executionContext.context))

    private fun checkForPlayServices(): Completable =
        Completable.fromAction {
            if (!playServicesUtil.isPlayServicesAvailable()) {
                throw ActionException {
                    // TODO: Localize
                    "Play Services are required to get the device's location, but they are not installed or not available."
                }
            }
        }

    private fun requestLocationPermissionIfNeeded(activity: FragmentActivity): Completable =
        Observable.defer {
            RxPermissions(activity)
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .flatMapCompletable { granted ->
                if (granted) {
                    Completable.complete()
                } else {
                    Completable.error(
                        ActionException { context ->
                            context.getString(R.string.error_failed_to_get_location)
                        }
                    )
                }
            }

    private fun fetchLocation(context: Context): Single<Any> =
        Single.create { emitter ->
            val cancellationTokenSource = CancellationTokenSource()
            emitter.setCancellable {
                cancellationTokenSource.cancel()
            }

            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(
                    CurrentLocationRequest.Builder()
                        .setDurationMillis(MAX_LOOKUP_TIME.inWholeMilliseconds)
                        .build(),
                    cancellationTokenSource.token,
                )
                .addOnSuccessListener { location: Location? ->
                    emitter.onSuccess(location.toResult())
                }
                .addOnCanceledListener {
                    emitter.onSuccess(NO_RESULT)
                }
                .addOnFailureListener { error ->
                    logException(error)
                    emitter.onError(
                        ActionException { context ->
                            context.getString(R.string.error_failed_to_get_location)
                        }
                    )
                }
        }

    companion object {
        private val MAX_LOOKUP_TIME = 20.seconds

        private fun Location?.toResult(): JSONObject =
            when {
                this != null -> {
                    createJSONObject(
                        status = "success",
                        latitude = latitude,
                        longitude = longitude,
                        accuracy = if (hasAccuracy()) accuracy else null,
                    )
                }
                else -> {
                    createJSONObject(
                        status = "unknown",
                    )
                }
            }

        private fun createJSONObject(
            status: String,
            latitude: Double? = null,
            longitude: Double? = null,
            accuracy: Float? = null,
        ): JSONObject =
            JSONObject(
                mapOf(
                    "status" to status,
                    "coordinates" to if (latitude != null && longitude != null) "$latitude,$longitude" else null,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "accuracy" to accuracy,
                )
            )
    }
}
