package ch.rmy.android.http_shortcuts.activities.settings.usecases

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.enums.AppIconType
import ch.rmy.android.launchers.AltIconBlackAndWhite
import ch.rmy.android.launchers.AltIconGreen
import ch.rmy.android.launchers.AltIconHacker
import ch.rmy.android.launchers.AltIconSolarized
import ch.rmy.android.launchers.AltIconWhiteAndBlack
import javax.inject.Inject
import kotlin.reflect.KClass

class SetAppIconUseCase
@Inject
constructor(
    private val context: Context,
) {
    private val packageManager = context.packageManager

    operator fun invoke(appIconType: AppIconType) {
        setComponent(appIconType.component, enabled = true)
        AppIconType.entries
            .filter { it != appIconType }
            .forEach {
                setComponent(it.component, enabled = false)
            }
    }

    private val AppIconType.component: KClass<*>
        get() = when (this) {
            AppIconType.DEFAULT -> MainActivity::class
            AppIconType.BLACK_AND_WHITE -> AltIconBlackAndWhite::class
            AppIconType.WHITE_AND_BLACK -> AltIconWhiteAndBlack::class
            AppIconType.SOLARIZED -> AltIconSolarized::class
            AppIconType.HACKER -> AltIconHacker::class
            AppIconType.GREEN -> AltIconGreen::class
        }

    private fun setComponent(component: KClass<*>, enabled: Boolean) {
        packageManager.setComponentEnabledSetting(
            ComponentName(context, component.java),
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    }
}
