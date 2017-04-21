package ch.rmy.android.http_shortcuts.utils;

public class ArrayUtil {

    public static int findIndex(Object[] items, Object item) {
        for (int i = 0; i < items.length; i++) {
            if (equals(items[i], item)) {
                return i;
            }
        }
        return 0;
    }

    private static boolean equals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    public static int findIndex(int[] items, int item) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == item) {
                return i;
            }
        }
        return 0;
    }

}
