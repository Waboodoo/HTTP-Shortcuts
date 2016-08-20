package ch.rmy.android.http_shortcuts.realm.models;

import android.content.Context;

import ch.rmy.android.http_shortcuts.R;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Variable extends RealmObject implements HasId {

    public static final String TYPE_CONSTANT = "constant";
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_SELECT = "select";
    public static final String TYPE_TOGGLE = "toggle";

    public static final String[] TYPE_OPTIONS = {
            TYPE_CONSTANT,
            TYPE_TEXT,
            TYPE_NUMBER,
            TYPE_SELECT,
            TYPE_TOGGLE
    };
    public static final int[] TYPE_RESOURCES = {
            R.string.variable_type_constant,
            R.string.variable_type_text,
            R.string.variable_type_number,
            R.string.variable_type_select,
            R.string.variable_type_toggle
    };

    @PrimaryKey
    private long id;

    @Required
    private String key;
    @Required
    private String type;

    private String value;
    private RealmList<Option> options;

    private boolean urlEncode;
    private boolean jsonEncode;

    @Required
    private String title;

    public static Variable createNew() {
        Variable variable = new Variable();
        variable.setKey("");
        variable.setType(TYPE_CONSTANT);
        variable.setValue("");
        variable.setTitle("");
        return variable;
    }

    @Override
    public boolean isNew() {
        return id == 0;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public RealmList<Option> getOptions() {
        return options;
    }

    public void setOptions(RealmList<Option> options) {
        this.options = options;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isUrlEncode() {
        return urlEncode;
    }

    public void setUrlEncode(boolean urlEncode) {
        this.urlEncode = urlEncode;
    }

    public boolean isJsonEncode() {
        return jsonEncode;
    }

    public void setJsonEncode(boolean jsonEncode) {
        this.jsonEncode = jsonEncode;
    }

    public static String[] getTypeOptions(Context context) {
        String[] typeStrings = new String[Variable.TYPE_OPTIONS.length];
        for (int i = 0; i < Variable.TYPE_OPTIONS.length; i++) {
            typeStrings[i] = context.getString(Variable.TYPE_RESOURCES[i]);
        }
        return typeStrings;
    }

}
