package ch.rmy.android.http_shortcuts.utils;

public class ArrayUtil {

    public static int findIndex(Object[] items, Object item) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(item)) {
                return i;
            }
        }
        return 0;
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
