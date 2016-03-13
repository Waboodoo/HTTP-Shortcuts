package ch.rmy.android.http_shortcuts.key_value_pairs;

public interface KeyValuePairFactory<T extends KeyValuePair> {

    T create(String key, String value);

}
