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

    public List<Shortcut> getShortcuts() {
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = dbHelper.getReadableDatabase();

            List<Shortcut> shortcuts = new ArrayList<Shortcut>();

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

    public Shortcut createShortcut() {
        return new Shortcut(0);
    }

    public long storeShortcut(Shortcut shortcut) {
        long shortcutID;
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(ShortcutTable.COLUMN_NAME, shortcut.getName());
            values.put(ShortcutTable.COLUMN_PROTOCOL, shortcut.getProtocol());
            values.put(ShortcutTable.COLUMN_URL, shortcut.getURL());
            values.put(ShortcutTable.COLUMN_METHOD, shortcut.getMethod());
            values.put(ShortcutTable.COLUMN_USERNAME, shortcut.getUsername());
            values.put(ShortcutTable.COLUMN_PASSWORD, shortcut.getPassword());
            values.put(ShortcutTable.COLUMN_FEEDBACK, shortcut.getFeedback());
            values.put(ShortcutTable.COLUMN_DESCRIPTION, shortcut.getDescription());
            values.put(ShortcutTable.COLUMN_BODY_CONTENT, shortcut.getBodyContent());
            values.put(ShortcutTable.COLUMN_TIMEOUT, shortcut.getTimeout());
            values.put(ShortcutTable.COLUMN_RETRY_POLICY, shortcut.getRetryPolicy());

            String iconName = null;
            if (shortcut.getIconName() != null) {
                iconName = shortcut.getIconName().toString();
            }
            values.put(ShortcutTable.COLUMN_ICON, iconName);

            if (shortcut.isNew()) {
                values.put(ShortcutTable.COLUMN_POSITION, getMaxPosition(database) + 1);
                shortcutID = database.insert(ShortcutTable.TABLE_NAME, null, values);
            } else {
                if (shortcut.getPosition() > 0 && shortcut.getPosition() <= getMaxPosition(database)) {
                    database.execSQL("update " + ShortcutTable.TABLE_NAME + " SET " + ShortcutTable.COLUMN_POSITION + " = " + ShortcutTable.COLUMN_POSITION + "+1 where "
                            + ShortcutTable.COLUMN_POSITION + " < (select position from " + ShortcutTable.TABLE_NAME + " where " + ShortcutTable.COLUMN_ID + " = "
                            + shortcut.getID() + ") AND " + ShortcutTable.COLUMN_POSITION + " >= " + shortcut.getPosition() + ";");

                    database.execSQL("update " + ShortcutTable.TABLE_NAME + " SET " + ShortcutTable.COLUMN_POSITION + " = " + ShortcutTable.COLUMN_POSITION + "-1 where "
                            + ShortcutTable.COLUMN_POSITION + " > (select position from " + ShortcutTable.TABLE_NAME + " where " + ShortcutTable.COLUMN_ID + " = "
                            + shortcut.getID() + ") AND " + ShortcutTable.COLUMN_POSITION + " <= " + shortcut.getPosition() + ";");

                    values.put(ShortcutTable.COLUMN_POSITION, shortcut.getPosition());
                }
                database.update(ShortcutTable.TABLE_NAME, values, ShortcutTable.COLUMN_ID + " = ?", new String[]{Long.toString(shortcut.getID())});
                shortcutID = shortcut.getID();
            }
        } finally {
            if (database != null) {
                database.close();
            }
        }

        return shortcutID;
    }

    public void deleteShortcut(Shortcut shortcut) {
        SQLiteDatabase database = null;
        try {
            database = dbHelper.getWritableDatabase();
            database.delete(ShortcutTable.TABLE_NAME, ShortcutTable.COLUMN_ID + " = ?", new String[]{Long.toString(shortcut.getID())});
            database.execSQL("update " + ShortcutTable.TABLE_NAME + " SET " + ShortcutTable.COLUMN_POSITION + " = " + ShortcutTable.COLUMN_POSITION + "-1 where "
                    + ShortcutTable.COLUMN_POSITION + " > " + shortcut.getPosition() + ";");
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    public Shortcut getShortcutByID(long shortcutID) {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = dbHelper.getReadableDatabase();

            cursor = database.query(ShortcutTable.TABLE_NAME, null, ShortcutTable.COLUMN_ID + " = ?", new String[]{Long.toString(shortcutID)}, null, null,
                    ShortcutTable.COLUMN_NAME + " ASC");
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return shortcutFromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }

        return null;
    }

    public List<PostParameter> getPostParametersByID(long shortcutID) {
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = dbHelper.getReadableDatabase();

            List<PostParameter> parameters = new ArrayList<PostParameter>();
            cursor = database.query(PostParameterTable.TABLE_NAME, null, PostParameterTable.COLUMN_SHORTCUT_ID + " = ?", new String[]{Long.toString(shortcutID)}, null, null,
                    null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                parameters.add(new PostParameter(cursor.getLong(0), cursor.getString(2), cursor.getString(3)));
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

    public void storePostParameters(long shortcutID, List<PostParameter> parameters) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        try {
            boolean first = true;
            StringBuilder allIDs = new StringBuilder();
            for (PostParameter param : parameters) {
                if (first) {
                    first = false;
                } else {
                    allIDs.append(',');
                }

                ContentValues values = new ContentValues();
                values.put(PostParameterTable.COLUMN_KEY, param.getKey());
                values.put(PostParameterTable.COLUMN_VALUE, param.getValue());
                values.put(PostParameterTable.COLUMN_SHORTCUT_ID, shortcutID);

                if (param.getID() == 0) {
                    // parameter is new -> create it
                    long newID = database.insert(PostParameterTable.TABLE_NAME, null, values);
                    allIDs.append(newID);

                } else {
                    // parameter is old -> update it
                    database.update(PostParameterTable.TABLE_NAME, values, PostParameterTable.COLUMN_ID + " = ?", new String[]{Long.toString(param.getID())});
                    allIDs.append(param.getID());

                }
            }

            // remove all deleted parameters
            database.delete(PostParameterTable.TABLE_NAME, PostParameterTable.COLUMN_SHORTCUT_ID + " = ? AND " + PostParameterTable.COLUMN_ID + " NOT IN (" + allIDs.toString()
                    + ")", new String[]{Long.toString(shortcutID)});

        } finally {

            if (database != null) {
                database.close();
            }
        }
    }

    public List<Header> getHeadersByID(long shortcutID) {
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = dbHelper.getReadableDatabase();

            List<Header> headers = new ArrayList<Header>();
            cursor = database.query(HeaderTable.TABLE_NAME, null, HeaderTable.COLUMN_SHORTCUT_ID + " = ?", new String[]{Long.toString(shortcutID)}, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                headers.add(new Header(cursor.getLong(0), cursor.getString(2), cursor.getString(3)));
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

    public void storeHeaders(long shortcutID, List<Header> headers) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        try {
            boolean first = true;
            StringBuilder allIDs = new StringBuilder();
            for (Header header : headers) {
                if (first) {
                    first = false;
                } else {
                    allIDs.append(',');
                }

                ContentValues values = new ContentValues();
                values.put(HeaderTable.COLUMN_KEY, header.getKey());
                values.put(HeaderTable.COLUMN_VALUE, header.getValue());
                values.put(HeaderTable.COLUMN_SHORTCUT_ID, shortcutID);

                if (header.getID() == 0) {
                    // header is new -> create it
                    long newID = database.insert(HeaderTable.TABLE_NAME, null, values);
                    allIDs.append(newID);

                } else {
                    // header is old -> update it
                    database.update(HeaderTable.TABLE_NAME, values, HeaderTable.COLUMN_ID + " = ?", new String[]{Long.toString(header.getID())});
                    allIDs.append(header.getID());

                }
            }

            // remove all deleted headers
            database.delete(HeaderTable.TABLE_NAME, HeaderTable.COLUMN_SHORTCUT_ID + " = ? AND " + HeaderTable.COLUMN_ID + " NOT IN (" + allIDs.toString() + ")",
                    new String[]{Long.toString(shortcutID)});

        } finally {

            if (database != null) {
                database.close();
            }
        }
    }

    public List<Shortcut> getShortcutsPendingExecution() {
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = dbHelper.getWritableDatabase();

            List<Shortcut> shortcuts = new ArrayList<Shortcut>();

            cursor = database.query(ShortcutTable.TABLE_NAME, null, ShortcutTable.COLUMN_RETRY + " = 1", null, null, null, ShortcutTable.COLUMN_POSITION + " ASC");
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                shortcuts.add(shortcutFromCursor(cursor));
                cursor.moveToNext();
            }

            ContentValues values = new ContentValues();
            values.put(ShortcutTable.COLUMN_RETRY, 0);
            database.update(ShortcutTable.TABLE_NAME, values, ShortcutTable.COLUMN_RETRY + " = 1", null);

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

    public void markShortcutAsPending(Shortcut shortcut) {
        SQLiteDatabase database = null;

        try {
            database = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ShortcutTable.COLUMN_RETRY, 1);
            database.update(ShortcutTable.TABLE_NAME, values, ShortcutTable.COLUMN_ID + " = ?", new String[]{Long.toString(shortcut.getID())});
        } finally {
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

    private int getMaxPosition(SQLiteDatabase database) {
        Cursor cursor = null;

        try {
            database = dbHelper.getReadableDatabase();
            cursor = database.query(ShortcutTable.TABLE_NAME, new String[]{ShortcutTable.COLUMN_POSITION}, null, null, null, null, ShortcutTable.COLUMN_POSITION + " DESC");

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    private Shortcut shortcutFromCursor(Cursor cursor) {
        Shortcut shortcut = new Shortcut(cursor.getLong(0));
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
