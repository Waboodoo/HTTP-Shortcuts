package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class WifiSSIDAction : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        Completable.defer {
            ensureLocationPermissionIsEnabled(executionContext.context as FragmentActivity)
        }
            .andThen(
                Single.fromCallable {
                    NetworkUtil.getCurrentSsid(executionContext.context)
                        ?: NO_RESULT
                }
            )

    private fun ensureLocationPermissionIsEnabled(activity: FragmentActivity): Completable =
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Completable.defer {
                DialogBuilder(activity)
                    .title(activity.getString(R.string.title_permission_dialog))
                    .message(activity.getString(R.string.message_permission_rational))
                    .positive(R.string.dialog_ok)
                    .showAsCompletable()
            }
        } else {
            Completable.complete()
        }
            .concatWith(requestLocationPermissionIfNeeded(activity))
            .subscribeOn(AndroidSchedulers.mainThread())

    private fun requestLocationPermissionIfNeeded(activity: FragmentActivity) =
        Observable.defer {
            RxPermissions(activity)
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
        }
            .flatMapCompletable { granted ->
                if (granted) {
                    Completable.complete()
                } else {
                    Completable.error(
                        ActionException { context ->
                            context.getString(R.string.error_failed_to_get_wifi_ssid)
                        }
                    )
                }
            }
}
