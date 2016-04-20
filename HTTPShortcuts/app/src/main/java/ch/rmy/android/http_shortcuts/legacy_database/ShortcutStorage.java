package ch.rmy.android.http_shortcuts.legacy_database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShortcutStorage {

    private DBCreator dbHelper;

    public ShortcutStorage(Context context) {
        dbHelper = new DBCreator(context);
    }

    public List<LegacyShortcut> getShortcuts() {
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = dbHelper.getReadableDatabase();

            List<LegacyShortcut> shortcuts = new ArrayList<LegacyShortcut>();

            cursor = database.query(ShortcutTable.TABLE_NAME, null, null, null, null, null, ShortcutTable.COLUMN_POSITION + " ASC");
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                shortcuts.add(shortcutFromCursor(cursor));
                cursor.moveToNext();
            }
            return shortcuts;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
    }

    public List<LegacyParameter> getPostParametersByID(long shortcutID) {
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = dbHelper.getReadableDatabase();

            List<LegacyParameter> parameters = new ArrayList<LegacyParameter>();
            cursor = database.query(PostParameterTable.TABLE_NAME, null, PostParameterTable.COLUMN_SHORTCUT_ID + " = ?", new String[]{Long.toString(shortcutID)}, null, null,
                    null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                parameters.add(new LegacyParameter(cursor.getLong(0), cursor.getString(2), cursor.getString(3)));
                cursor.moveToNext();
            }
            return parameters;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
    }

    public List<LegacyHeader> getHeadersByID(long shortcutID) {
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = dbHelper.getReadableDatabase();

            List<LegacyHeader> headers = new ArrayList<LegacyHeader>();
            cursor = database.query(HeaderTable.TABLE_NAME, null, HeaderTable.COLUMN_SHORTCUT_ID + " = ?", new String[]{Long.toString(shortcutID)}, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                headers.add(new LegacyHeader(cursor.getLong(0), cursor.getString(2), cursor.getString(3)));
                cursor.moveToNext();
            }
            return headers;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
    }

    public File getDatabaseFile() {
        SQLiteDatabase database = null;

        try {
            database = dbHelper.getReadableDatabase();
            return new File(database.getPath());
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    private LegacyShortcut shortcutFromCursor(Cursor cursor) {
        LegacyShortcut shortcut = new LegacyShortcut(cursor.getLong(0));
        shortcut.setName(cursor.getString(1));
        shortcut.setDescription(cursor.getString(11));
        shortcut.setProtocol(cursor.getString(2));
        shortcut.setURL(cursor.getString(3));
        shortcut.setMethod(cursor.getString(4));
        shortcut.setUsername(cursor.getString(5));
        shortcut.setPassword(cursor.getString(6));
        shortcut.setIconName(cursor.getString(7));
        shortcut.setFeedback(cursor.getInt(8));
        shortcut.setPosition(cursor.getInt(10));
        shortcut.setBodyContent(cursor.getString(12));
        shortcut.setRetryPolicy(cursor.getInt(15));
        shortcut.setTimeout(cursor.getInt(13));
        return shortcut;
    }

}
