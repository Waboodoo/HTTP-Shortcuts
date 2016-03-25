package ch.rmy.android.http_shortcuts.realm.models;

import android.content.Context;
import android.net.Uri;

import ch.rmy.android.http_shortcuts.R;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Shortcut extends RealmObject {

    public static final String FIELD_ID = "id";
    public static final String FIELD_POSITION = "position";
    public static final String FIELD_RETRY_STATUS = "retryStatus";

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PATCH = "PATCH";

    public static final String FEEDBACK_NONE = "none";
    public static final String FEEDBACK_ERRORS_ONLY = "errors_only";
    public static final String FEEDBACK_SIMPLE = "simple_response";
    public static final String FEEDBACK_FULL_RESPONSE = "full_response";

    public static final String RETRY_POLICY_NONE = "none";
    public static final String RETRY_POLICY_WAIT_FOR_INTERNET = "wait_for_internet";

    public static final String RETRY_STATUS_WAITING = "waiting";

    public static final int DEFAULT_ICON = R.drawable.ic_launcher;

    public static final String[] METHOD_OPTIONS = {METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE, METHOD_PATCH};
    public static final String[] FEEDBACK_OPTIONS = {FEEDBACK_NONE, FEEDBACK_ERRORS_ONLY, FEEDBACK_SIMPLE, FEEDBACK_FULL_RESPONSE};
    public static final int[] FEEDBACK_RESOURCES = {R.string.feedback_none, R.string.feedback_errors_only, R.string.feedback_simple, R.string.feedback_full_response};
    public static final int[] TIMEOUT_OPTIONS = {3000, 10000, 30000, 60000};
    public static final int[] TIMEOUT_RESOURCES = {R.string.timeout_short, R.string.timeout_medium, R.string.timeout_long, R.string.timeout_very_long};
    public static final String[] RETRY_POLICY_OPTIONS = {RETRY_POLICY_NONE, RETRY_POLICY_WAIT_FOR_INTERNET};
    public static final int[] RETRY_POLICY_RESOURCES = {R.string.retry_policy_none, R.string.retry_policy_delayed};

    @PrimaryKey
    private long id;
    @Required
    private String name;
    @Required
    private String method = METHOD_GET;
    @Required
    private String url;
    @Required
    private String username;
    @Required
    private String password;
    private String iconName;
    @Required
    private String feedback;
    private int position;
    @Required
    private String description;
    @Required
    private String bodyContent;
    private int timeout;
    @Required
    private String retryPolicy;
    private RealmList<Header> headers;
    private RealmList<Parameter> parameters;

    private String retryStatus;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(String retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public RealmList<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(RealmList<Header> headers) {
        this.headers = headers;
    }

    public RealmList<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(RealmList<Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(String retryStatus) {
        this.retryStatus = retryStatus;
    }

    public boolean isNew() {
        return id == 0;
    }

    public static Shortcut createNew() {
        Shortcut shortcut = new Shortcut();
        shortcut.setId(0);
        shortcut.setMethod(METHOD_GET);
        shortcut.setUrl("http://");
        shortcut.setTimeout(TIMEOUT_OPTIONS[1]);
        shortcut.setFeedback(FEEDBACK_SIMPLE);
        shortcut.setRetryPolicy(RETRY_POLICY_NONE);
        shortcut.setParameters(new RealmList<Parameter>());
        shortcut.setHeaders(new RealmList<Header>());
        return shortcut;
    }

    public Shortcut duplicate(String newName) {
        Shortcut duplicate = new Shortcut();
        duplicate.setId(0);
        duplicate.setName(newName);
        duplicate.setBodyContent(getBodyContent());
        duplicate.setDescription(getDescription());
        duplicate.setFeedback(getFeedback());
        duplicate.setIconName(getIconName());
        duplicate.setMethod(getMethod());
        duplicate.setPassword(getPassword());
        duplicate.setRetryPolicy(getRetryPolicy());
        duplicate.setTimeout(getTimeout());
        duplicate.setUrl(getUrl());
        duplicate.setUsername(getUsername());
        duplicate.setPosition(getPosition() + 1);
        return duplicate;
    }

    public Uri getIconURI(Context context) {
        String packageName = context.getPackageName();
        if (iconName == null) {
            return Uri.parse("android.resource://" + packageName + "/" + DEFAULT_ICON);
        } else if (iconName.startsWith("android.resource://")) {
            return Uri.parse(iconName);
        } else if (iconName.endsWith(".png")) {
            return Uri.fromFile(context.getFileStreamPath(iconName));
        } else {
            int identifier = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
            return Uri.parse("android.resource://" + packageName + "/" + identifier);
        }
    }

}
