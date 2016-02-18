package ch.rmy.android.http_shortcuts.shortcuts;

public class PostParameter {

    private final long id;
    private String key;
    private String value;

    public PostParameter(long id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public long getID() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
