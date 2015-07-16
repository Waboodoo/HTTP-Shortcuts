package ch.rmy.android.http_shortcuts.shortcuts;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

			cursor = database.query(Table.TABLE_NAME, null, null, null, null, null, Table.COLUMN_POSITION + " ASC");
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				shortcuts.add(new Shortcut(cursor));
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
		return new Shortcut(0, "", "", Shortcut.PROTOCOL_HTTP, "", Shortcut.METHOD_GET, "", "", null, Shortcut.FEEDBACK_SIMPLE, 0);
	}

	public void storeShortcut(Shortcut shortcut) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		try {
			ContentValues values = new ContentValues();
			values.put(Table.COLUMN_NAME, shortcut.getName());
			values.put(Table.COLUMN_PROTOCOL, shortcut.getProtocol());
			values.put(Table.COLUMN_URL, shortcut.getURL());
			values.put(Table.COLUMN_METHOD, shortcut.getMethod());
			values.put(Table.COLUMN_USERNAME, shortcut.getUsername());
			values.put(Table.COLUMN_PASSWORD, shortcut.getPassword());
			values.put(Table.COLUMN_FEEDBACK, shortcut.getFeedback());
			values.put(Table.COLUMN_DESCRIPTION, shortcut.getDescription());

			String iconName = null;
			if (shortcut.getIconName() != null) {
				iconName = shortcut.getIconName().toString();
			}
			values.put(Table.COLUMN_ICON, iconName);

			if (shortcut.isNew()) {
				values.put(Table.COLUMN_POSITION, getMaxPosition(database) + 1);
				database.insert(Table.TABLE_NAME, null, values);
			} else {
				if (shortcut.getPosition() > 0 && shortcut.getPosition() <= getMaxPosition(database)) {
					database.execSQL("update " + Table.TABLE_NAME + " SET " + Table.COLUMN_POSITION + " = " + Table.COLUMN_POSITION + "+1 where " + Table.COLUMN_POSITION
							+ " < (select position from " + Table.TABLE_NAME + " where " + Table.COLUMN_ID + " = " + shortcut.getID() + ") AND " + Table.COLUMN_POSITION + " >= "
							+ shortcut.getPosition() + ";");

					database.execSQL("update " + Table.TABLE_NAME + " SET " + Table.COLUMN_POSITION + " = " + Table.COLUMN_POSITION + "-1 where " + Table.COLUMN_POSITION
							+ " > (select position from " + Table.TABLE_NAME + " where " + Table.COLUMN_ID + " = " + shortcut.getID() + ") AND " + Table.COLUMN_POSITION + " <= "
							+ shortcut.getPosition() + ";");

					values.put(Table.COLUMN_POSITION, shortcut.getPosition());
				}
				database.update(Table.TABLE_NAME, values, Table.COLUMN_ID + " = ?", new String[] { Integer.toString(shortcut.getID()) });
			}
		} finally {
			if (database != null) {
				database.close();
			}
		}
	}

	public void deleteShortcut(Shortcut shortcut) {
		SQLiteDatabase database = null;
		try {
			database = dbHelper.getWritableDatabase();
			database.delete(Table.TABLE_NAME, Table.COLUMN_ID + " = ?", new String[] { Integer.toString(shortcut.getID()) });
			database.execSQL("update " + Table.TABLE_NAME + " SET " + Table.COLUMN_POSITION + " = " + Table.COLUMN_POSITION + "-1 where " + Table.COLUMN_POSITION + " > "
					+ shortcut.getPosition() + ";");
		} finally {
			if (database != null) {
				database.close();
			}
		}
	}

	public Shortcut getShortcutByID(int shortcutID) {
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = dbHelper.getReadableDatabase();

			cursor = database.query(Table.TABLE_NAME, null, Table.COLUMN_ID + " = ?", new String[] { Integer.toString(shortcutID) }, null, null, Table.COLUMN_NAME + " ASC");
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				return new Shortcut(cursor);
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

	private int getMaxPosition(SQLiteDatabase database) {
		Cursor cursor = null;

		try {
			database = dbHelper.getReadableDatabase();
			cursor = database.query(Table.TABLE_NAME, new String[] { Table.COLUMN_POSITION }, null, null, null, null, Table.COLUMN_POSITION + " DESC");

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

}
