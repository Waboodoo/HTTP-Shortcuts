package ch.rmy.android.http_shortcuts.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import io.realm.RealmObject;

public class GsonUtil {

    public static String toJson(Shortcut shortcut) {
        Gson gson = getJsonBuilder().create();
        return gson.toJson(shortcut);
    }

    public static Shortcut fromJson(String json) {
        Gson gson = getJsonBuilder().create();
        return gson.fromJson(json, Shortcut.class);
    }

    public static void export(Object object, Appendable writer) {
        Gson gson = getJsonBuilder().setPrettyPrinting().create();
        gson.toJson(object, writer);
    }

    private static GsonBuilder getJsonBuilder() {
        return (new GsonBuilder()).addSerializationExclusionStrategy(new RealmExclusionStrategy());
    }

    private static class RealmExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getDeclaringClass().equals(RealmObject.class);
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }

    }

}
