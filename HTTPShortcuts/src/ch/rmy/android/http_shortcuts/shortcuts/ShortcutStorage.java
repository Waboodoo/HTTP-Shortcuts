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
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		List<Shortcut> shortcuts = new ArrayList<Shortcut>();

		Cursor cursor = database.query(Table.TABLE_NAME, null, null, null, null, null, Table.COLUMN_NAME + " ASC");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {

			int id = cursor.getInt(0);
			String name = cursor.getString(1);
			String protocol = cursor.getString(2);
			String url = cursor.getString(3);
			String method = cursor.getString(4);
			String username = cursor.getString(5);
			String password = cursor.getString(6);
			String iconName = cursor.getString(7);
			int feedback = cursor.getInt(8);

			shortcuts.add(new Shortcut(id, name, protocol, url, method, username, password, iconName, feedback));

			cursor.moveToNext();
		}
		cursor.close();

		database.close();

		return shortcuts;
	}

	public Shortcut createShortcut() {
		return new Shortcut(0, "", Shortcut.PROTOCOL_HTTP, "", Shortcut.METHOD_GET, "", "", null, Shortcut.FEEDBACK_SIMPLE);
	}

	public void storeShortcut(Shortcut shortcut) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Table.COLUMN_NAME, shortcut.getName());
		values.put(Table.COLUMN_PROTOCOL, shortcut.getProtocol());
		values.put(Table.COLUMN_URL, shortcut.getURL());
		values.put(Table.COLUMN_METHOD, shortcut.getMethod());
		values.put(Table.COLUMN_USERNAME, shortcut.getUsername());
		values.put(Table.COLUMN_PASSWORD, shortcut.getPassword());
		values.put(Table.COLUMN_FEEDBACK, shortcut.getFeedback());

		String iconName = null;
		if (shortcut.getIconName() != null) {
			iconName = shortcut.getIconName().toString();
		}
		values.put(Table.COLUMN_ICON, iconName);

		if (shortcut.isNew()) {
			database.insert(Table.TABLE_NAME, null, values);
		} else {
			database.update(Table.TABLE_NAME, values, Table.COLUMN_ID + " = ?", new String[] { Integer.toString(shortcut.getID()) });
		}

		database.close();
	}

	public void deleteShortcut(Shortcut shortcut) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		database.delete(Table.TABLE_NAME, Table.COLUMN_ID + " = ?", new String[] { Integer.toString(shortcut.getID()) });

		database.close();
	}

	public Shortcut getShortcutByID(int shortcutID) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor = database.query(Table.TABLE_NAME, null, Table.COLUMN_ID + " = ?", new String[] { Integer.toString(shortcutID) }, null, null, Table.COLUMN_NAME + " ASC");

		try {
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {

				int id = cursor.getInt(0);
				String name = cursor.getString(1);
				String protocol = cursor.getString(2);
				String url = cursor.getString(3);
				String method = cursor.getString(4);
				String username = cursor.getString(5);
				String password = cursor.getString(6);
				String iconName = cursor.getString(7);
				int feedback = cursor.getInt(8);

				return new Shortcut(id, name, protocol, url, method, username, password, iconName, feedback);
			}
		} finally {
			cursor.close();
			database.close();
		}

		return null;
	}

}
