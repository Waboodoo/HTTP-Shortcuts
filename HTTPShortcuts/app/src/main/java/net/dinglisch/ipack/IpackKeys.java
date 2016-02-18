package net.dinglisch.ipack;

public class IpackKeys {

    public final static String RECEIVER_NAME = "IpackReceiver";
    public final static String SELECTOR_NAME = "IpackIconSelect";

    public static final String SCHEME = "ipack";
    public static final String PREFIX = SCHEME + "://";

    private final static String PACKAGE_NAME = "net.dinglisch.android.ipack";
    public final static String ANDROID_RESOURCE_SCHEME = "android.resource";
    public final static String ANDROID_RESOURCE_PREFIX = ANDROID_RESOURCE_SCHEME + "://";

    public final static String ICON_DOWNLOAD_URI_DIRECT = "http://ipack.dinglisch.net/download.html";
    public final static String ICON_DOWNLOAD_URI_MARKET = "market://search?q=ipack";

    // http://market.android.com/search?q=<substring> or

    public class Actions {

        private final static String PREFIX = PACKAGE_NAME + ".actions.";

        public final static String ICON_SELECT = PREFIX + "ICON_SELECT";
        public final static String QUERY_PACKS = PREFIX + "QUERY_PACKS";
        public final static String QUERY_ICONS = PREFIX + "QUERY_ICONS";
        public final static String NOTIFY = PREFIX + "NOTIFY";
        public final static String NOTIFY_CANCEL = PREFIX + "NOTIFY_CANCEL";
    }

    public class Extras {
        private final static String PREFIX = PACKAGE_NAME + ".extras.";

        public final static String CELL_SIZE = PREFIX + "CELL_SIZE";
        public final static String ICON_DISPLAY_SIZE = PREFIX + "ICON_DISPLAY_SIZE";
        public final static String GRID_BACK_COLOUR = PREFIX + "GRID_BACK_COLOUR";

        public final static String ICON_LABEL = PREFIX + "ICON_LABEL";
        public final static String ICON_NAME = PREFIX + "ICON_NAME";
        public final static String ICON_ID = PREFIX + "ICON_ID";

        public final static String LABEL = PREFIX + "LABEL";
        public final static String ALL_SAME_SIZE = PREFIX + "ALL_SAME_SIZE";
        public final static String ATTRIBUTION = PREFIX + "ATTRIBUTION";

        public final static String NOTIFICATION_ID = PREFIX + "NOTIFICATION_ID";
        public final static String NOTIFICATION = PREFIX + "NOTIFICATION";
        public final static String NOTIFICATION_TITLE = PREFIX + "NOTIFICATION_TITLE";
        public final static String NOTIFICATION_TEXT = PREFIX + "NOTIFICATION_TEXT";
        public final static String NOTIFICATION_PI = PREFIX + "NOTIFICATION_PI";
    }
}