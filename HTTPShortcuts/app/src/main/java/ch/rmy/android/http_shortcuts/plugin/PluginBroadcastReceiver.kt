package ch.rmy.android.http_shortcuts.plugin

import android.content.Context
import android.os.Bundle
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginSettingReceiver

class PluginBroadcastReceiver : AbstractPluginSettingReceiver() {

    override fun isBundleValid(bundle: Bundle) = PluginBundleManager.isBundleValid(bundle)

    override fun isAsync() = false

    override fun firePluginSetting(context: Context, bundle: Bundle) {
        val shortcutId = PluginBundleManager.getShortcutId(bundle)
        val variableValues = PluginBundleManager.getVariableValues(bundle)
        val intent = ExecuteActivity.IntentBuilder(context, shortcutId)
                .variableValues(variableValues)
                .build()
        context.startActivity(intent)
    }

}
