package ch.rmy.android.http_shortcuts.plugin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginSettingReceiver;

import ch.rmy.android.http_shortcuts.utils.IntentFactory;

public final class PluginBroadcastReceiver extends AbstractPluginSettingReceiver {

    @Override
    protected boolean isBundleValid(@NonNull final Bundle bundle) {
        return PluginBundleManager.isBundleValid(bundle);
    }

    @Override
    protected boolean isAsync() {
        return false;
    }

    @Override
    protected void firePluginSetting(@NonNull final Context context, @NonNull final Bundle bundle) {
        long shortcutId = PluginBundleManager.getShortcutId(bundle);
        Intent intent = IntentFactory.createIntent(context, shortcutId);
        context.startActivity(intent);
    }

}