package ch.rmy.android.http_shortcuts.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.Reader;

import ch.rmy.android.http_shortcuts.realm.models.Base;
import io.realm.RealmObject;

public class GsonUtil {

    public static String prettyPrint(String jsonString) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(jsonString).getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(json);
        } catch (JsonParseException e) {
            return jsonString;
        }
    }

    public static String toJson(RealmObject item) {
        Gson gson = getJsonBuilder().create();
        return gson.toJson(item);
    }

    public static <T extends RealmObject> T fromJson(String json, Class<T> clazz) {
        Gson gson = getJsonBuilder().create();
        return gson.fromJson(json, clazz);
    }

    public static void exportData(Base base, Appendable writer) {
        Gson gson = getJsonBuilder().setPrettyPrinting().create();
        gson.toJson(base, writer);
    }

    public static String exportData(Base base) {
        Gson gson = getJsonBuilder().setPrettyPrinting().create();
        return gson.toJson(base);
    }

    public static Base importData(Reader reader) {
        Gson gson = getJsonBuilder().create();
        return gson.fromJson(reader, Base.class);
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
