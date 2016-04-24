package ch.rmy.android.http_shortcuts.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class GsonUtil {

    public static String toJson(Shortcut shortcut) {
        Gson gson = getJsonBuilder().create();
        return gson.toJson(shortcut);
    }

    public static Shortcut fromJson(String json) {
        Gson gson = getJsonBuilder().create();
        return gson.fromJson(json, Shortcut.class);
    }

    private static GsonBuilder getJsonBuilder() {
        return (new GsonBuilder()).addSerializationExclusionStrategy(new RealmExclusionStrategy());
    }

    private static class RealmExclusionStrategy implements ExclusionStrategy {

        private static final String[] EXCLUDED_FIELDS = {
                "listeners", "currentTableVersion", "isCompleted"
        };

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            for (String fieldName : EXCLUDED_FIELDS) {
                if (fieldName.equals(f.getName())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }

}
