package ch.rmy.android.http_shortcuts.legacy_database;

import android.content.Context;
import android.net.Uri;

import ch.rmy.android.http_shortcuts.R;

public class Shortcut {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PATCH = "PATCH";

    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";

    public static final int FEEDBACK_NONE = 0;
    public static final int FEEDBACK_ERRORS_ONLY = 1;
    public static final int FEEDBACK_SIMPLE = 2;
    public static final int FEEDBACK_FULL_RESPONSE = 3;

    public static final int RETRY_POLICY_NONE = 0;
    public static final int RETRY_POLICY_WAIT_FOR_INTERNET = 1;

    public static final int DEFAULT_ICON = R.drawable.ic_launcher;

    public static final String[] METHODS = {METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE, METHOD_PATCH};
    public static final String[] PROTOCOLS = {PROTOCOL_HTTP, PROTOCOL_HTTPS};
    public static final int[] FEEDBACK_OPTIONS = {FEEDBACK_NONE, FEEDBACK_ERRORS_ONLY, FEEDBACK_SIMPLE, FEEDBACK_FULL_RESPONSE};
    public static final int[] FEEDBACK_RESOURCES = {R.string.feedback_none, R.string.feedback_errors_only, R.string.feedback_simple, R.string.feedback_full_response};
    public static final int[] TIMEOUT_OPTIONS = {3000, 10000, 30000, 60000};
    public static final int[] TIMEOUT_RESOURCES = {R.string.timeout_short, R.string.timeout_medium, R.string.timeout_long, R.string.timeout_very_long};
    public static final int[] RETRY_POLICY_OPTIONS = {RETRY_POLICY_NONE, RETRY_POLICY_WAIT_FOR_INTERNET};
    public static final int[] RETRY_POLICY_RESOURCES = {R.string.retry_policy_none, R.string.retry_policy_delayed};

    private final long id;
    private String name = "";
    private String method = METHOD_GET;
    private String protocol = PROTOCOL_HTTP;
    private String url = "";
    private String username = "";
    private String password = "";
    private String iconName = null;
    private int feedback = FEEDBACK_SIMPLE;
    private int position = 0;
    private String description = "";
    private String bodyContent;
    private int timeout = Shortcut.TIMEOUT_OPTIONS[0];
    private int retryPolicy = RETRY_POLICY_NONE;

    protected Shortcut(long id) {
        this.id = id;
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getURL() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getIconName() {
        return iconName;
    }

    public int getFeedback() {
        return feedback;
    }

    public int getPosition() {
        return position;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setProtocol(String protocol) {
        this.protocol = PROTOCOL_HTTPS.equals(protocol) ? PROTOCOL_HTTPS : PROTOCOL_HTTP;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public void setFeedback(int feedback) {
        this.feedback = feedback;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setBodyContent(String bodyContent) {
        if (bodyContent == null) {
            this.bodyContent = "";
        } else {
            this.bodyContent = bodyContent;
        }
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        if (timeout <= 0) {
            this.timeout = TIMEOUT_OPTIONS[0];
        } else {
            this.timeout = timeout;
        }
    }

    public int getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(int retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public boolean isNew() {
        return id == 0;
    }

    public Shortcut duplicate(String newName) {
        Shortcut duplicate = new Shortcut(0);
        duplicate.setName(newName);
        duplicate.setBodyContent(bodyContent);
        duplicate.setDescription(description);
        duplicate.setFeedback(feedback);
        duplicate.setIconName(iconName);
        duplicate.setMethod(method);
        duplicate.setPassword(password);
        duplicate.setProtocol(protocol);
        duplicate.setRetryPolicy(retryPolicy);
        duplicate.setTimeout(timeout);
        duplicate.setURL(url);
        duplicate.setUsername(username);
        return duplicate;
    }

    public Uri getIconURI(Context context) {
        if (iconName == null) {
            return Uri.parse("android.resource://" + context.getPackageName() + "/" + DEFAULT_ICON);
        } else if (iconName.startsWith("android.resource://")) {
            return Uri.parse(iconName);
        } else if (iconName.endsWith(".png")) {
            return Uri.fromFile(context.getFileStreamPath(iconName));
        } else {
            return Uri.parse("android.resource://" + context.getPackageName() + "/" + context.getResources().getIdentifier(iconName, "drawable", context.getPackageName()));
        }
    }

}
