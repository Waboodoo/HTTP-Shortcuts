package ch.rmy.android.http_shortcuts.realm.models;

import android.content.Context;
import android.net.Uri;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.utils.ShortcutUIUtils;
import ch.rmy.android.http_shortcuts.utils.Validation;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Shortcut extends RealmObject implements HasId {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PATCH = "PATCH";

    public static final String FEEDBACK_NONE = "none";
    public static final String FEEDBACK_TOAST_SIMPLE = "simple_response";
    public static final String FEEDBACK_TOAST_SIMPLE_ERRORS = "simple_response_errors";
    public static final String FEEDBACK_TOAST = "full_response";
    public static final String FEEDBACK_TOAST_ERRORS = "errors_only";
    public static final String FEEDBACK_DIALOG = "dialog";
    public static final String FEEDBACK_ACTIVITY = "activity";

    public static final String RETRY_POLICY_NONE = "none";
    public static final String RETRY_POLICY_WAIT_FOR_INTERNET = "wait_for_internet";

    public static final String[] METHOD_OPTIONS = {METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE, METHOD_PATCH};
    public static final String[] FEEDBACK_OPTIONS = {FEEDBACK_NONE, FEEDBACK_TOAST_SIMPLE, FEEDBACK_TOAST_SIMPLE_ERRORS, FEEDBACK_TOAST, FEEDBACK_TOAST_ERRORS, FEEDBACK_DIALOG, FEEDBACK_ACTIVITY};
    public static final int[] TIMEOUT_OPTIONS = {3000, 10000, 30000, 60000};

    public static final String[] RETRY_POLICY_OPTIONS = {RETRY_POLICY_NONE, RETRY_POLICY_WAIT_FOR_INTERNET};

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
    @Required
    private String description;
    @Required
    private String bodyContent;
    private int timeout;
    @Required
    private String retryPolicy;
    private RealmList<Header> headers;
    private RealmList<Parameter> parameters;
    private boolean acceptAllCertificates;

    @Override
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

    public boolean isAcceptAllCertificates() {
        return acceptAllCertificates;
    }

    public void setAcceptAllCertificates(boolean acceptAllCertificates) {
        this.acceptAllCertificates = acceptAllCertificates;
    }

    @Override
    public boolean isNew() {
        return id == 0;
    }

    public static Shortcut createNew() {
        Shortcut shortcut = new Shortcut();
        shortcut.setId(0);
        shortcut.setName("");
        shortcut.setDescription("");
        shortcut.setUsername("");
        shortcut.setPassword("");
        shortcut.setBodyContent("");
        shortcut.setMethod(METHOD_GET);
        shortcut.setUrl("http://");
        shortcut.setTimeout(TIMEOUT_OPTIONS[1]);
        shortcut.setFeedback(FEEDBACK_TOAST_SIMPLE);
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

        duplicate.setParameters(new RealmList<Parameter>());
        for (Parameter parameter : getParameters()) {
            duplicate.getParameters().add(Parameter.createNew(parameter.getKey(), parameter.getValue()));
        }

        duplicate.setHeaders(new RealmList<Header>());
        for (Header header : getHeaders()) {
            duplicate.getHeaders().add(Header.createNew(header.getKey(), header.getValue()));
        }

        return duplicate;
    }

    public Uri getIconURI(Context context) {
        String packageName = context.getPackageName();
        if (iconName == null) {
            return Uri.parse("android.resource://" + packageName + "/" + ShortcutUIUtils.DEFAULT_ICON);
        } else if (iconName.startsWith("android.resource://")) {
            return Uri.parse(iconName);
        } else if (iconName.endsWith(".png")) {
            return Uri.fromFile(context.getFileStreamPath(iconName));
        } else {
            int identifier = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
            return Uri.parse("android.resource://" + packageName + "/" + identifier);
        }
    }

    public String getSafeName(Context context) {
        if (Validation.isEmpty(name)) {
            return context.getString(R.string.shortcut_safe_name);
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Shortcut shortcut = (Shortcut) o;

        if (getTimeout() != shortcut.getTimeout()) return false;
        if (!getName().equals(shortcut.getName())) return false;
        if (!getMethod().equals(shortcut.getMethod())) return false;
        if (!getUrl().equals(shortcut.getUrl())) return false;
        if (!getUsername().equals(shortcut.getUsername())) return false;
        if (!getPassword().equals(shortcut.getPassword())) return false;
        if (getIconName() != null ? !getIconName().equals(shortcut.getIconName()) : shortcut.getIconName() != null)
            return false;
        if (!getFeedback().equals(shortcut.getFeedback())) return false;
        if (!getDescription().equals(shortcut.getDescription())) return false;
        if (!getBodyContent().equals(shortcut.getBodyContent())) return false;
        if (!getRetryPolicy().equals(shortcut.getRetryPolicy())) return false;
        if (!getHeaders().equals(shortcut.getHeaders())) return false;
        return getParameters().equals(shortcut.getParameters());
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + getName().hashCode();
        result = 31 * result + getMethod().hashCode();
        result = 31 * result + getUrl().hashCode();
        result = 31 * result + getUsername().hashCode();
        result = 31 * result + getPassword().hashCode();
        result = 31 * result + (getIconName() != null ? getIconName().hashCode() : 0);
        result = 31 * result + getFeedback().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getBodyContent().hashCode();
        result = 31 * result + getTimeout();
        result = 31 * result + getRetryPolicy().hashCode();
        result = 31 * result + getHeaders().hashCode();
        result = 31 * result + getParameters().hashCode();
        return result;
    }

    public boolean feedbackUsesUI() {
        return FEEDBACK_DIALOG.equals(getFeedback()) || FEEDBACK_ACTIVITY.equals(getFeedback());
    }

    public boolean isFeedbackErrorsOnly() {
        return FEEDBACK_TOAST_ERRORS.equals(getFeedback()) || FEEDBACK_TOAST_SIMPLE_ERRORS.equals(getFeedback());
    }

    public boolean isRetryAllowed() {
        return !FEEDBACK_ACTIVITY.equals(getFeedback()) && !FEEDBACK_DIALOG.equals(getFeedback());
    }
}
