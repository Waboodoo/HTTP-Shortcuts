package ch.rmy.android.http_shortcuts.legacy_database;

import android.content.Context;
import android.util.Log;

import java.util.List;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class Migration {

    private static final String TAG = Migration.class.getName();

    private final Context context;
    private final Controller controller;

    public Migration(Context context, Controller controller) {
        this.context = context;
        this.controller = controller;
    }

    public void migrate() {
        Category category = controller.getCategories().get(0);

        ShortcutStorage storage = new ShortcutStorage(context);
        for (LegacyShortcut oldShortcut : storage.getShortcuts()) {
            List<LegacyHeader> headers = storage.getHeadersByID(oldShortcut.getID());
            List<LegacyParameter> parameters = storage.getPostParametersByID(oldShortcut.getID());

            Shortcut newShortcut = migrateShortcut(oldShortcut, headers, parameters);

            Shortcut persistedShortcut = controller.persist(newShortcut);
            controller.moveShortcut(persistedShortcut, category);
        }

        Log.d(TAG, "deleted: " + storage.getDatabaseFile().delete());
    }

    private Shortcut migrateShortcut(LegacyShortcut oldShortcut, List<LegacyHeader> oldHeaders, List<LegacyParameter> oldParameters) {
        Shortcut shortcut = Shortcut.createNew();
        shortcut.setId(oldShortcut.getID());
        shortcut.setName(sanitize(oldShortcut.getName()));
        shortcut.setUrl(oldShortcut.getProtocol() + "://" + oldShortcut.getURL());
        shortcut.setDescription(sanitize(oldShortcut.getDescription()));
        shortcut.setTimeout(oldShortcut.getTimeout());
        shortcut.setBodyContent(sanitize(oldShortcut.getBodyContent()));
        shortcut.setIconName(oldShortcut.getIconName());
        shortcut.setUsername(sanitize(oldShortcut.getUsername()));
        shortcut.setPassword(sanitize(oldShortcut.getPassword()));
        shortcut.setMethod(oldShortcut.getMethod());

        switch (oldShortcut.getFeedback()) {
            case LegacyShortcut.FEEDBACK_NONE:
                shortcut.setFeedback(Shortcut.FEEDBACK_NONE);
                break;
            case LegacyShortcut.FEEDBACK_ERRORS_ONLY:
                shortcut.setFeedback(Shortcut.FEEDBACK_ERRORS_ONLY);
                break;
            case LegacyShortcut.FEEDBACK_SIMPLE:
                shortcut.setFeedback(Shortcut.FEEDBACK_SIMPLE);
                break;
            case LegacyShortcut.FEEDBACK_FULL_RESPONSE:
                shortcut.setFeedback(Shortcut.FEEDBACK_FULL_RESPONSE);
                break;
        }

        switch (oldShortcut.getRetryPolicy()) {
            case LegacyShortcut.RETRY_POLICY_NONE:
                shortcut.setRetryPolicy(Shortcut.RETRY_POLICY_NONE);
                break;
            case LegacyShortcut.RETRY_POLICY_WAIT_FOR_INTERNET:
                shortcut.setRetryPolicy(Shortcut.RETRY_POLICY_WAIT_FOR_INTERNET);
                break;
        }

        for (LegacyHeader oldHeader : oldHeaders) {
            shortcut.getHeaders().add(migrateHeader(oldHeader));
        }

        for (LegacyParameter oldParameter : oldParameters) {
            shortcut.getParameters().add(migrateParameter(oldParameter));
        }

        return shortcut;
    }

    private Header migrateHeader(LegacyHeader oldHeader) {
        Header header = new Header();
        header.setKey(oldHeader.getKey());
        header.setValue(oldHeader.getValue());
        return header;
    }

    private Parameter migrateParameter(LegacyParameter oldParameter) {
        Parameter parameter = new Parameter();
        parameter.setKey(oldParameter.getKey());
        parameter.setValue(oldParameter.getValue());
        return parameter;
    }

    private String sanitize(String string) {
        if (string == null) {
            return "";
        }
        return string;
    }

}
