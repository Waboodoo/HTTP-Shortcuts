package ch.rmy.android.http_shortcuts.shortcuts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBCreator extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "shortcuts.db";

	public DBCreator(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Table.getCreateStatement());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (int version = oldVersion + 1; version <= newVersion; version++) {
			for (String statement : Table.getUpdateStatements(version)) {
				db.execSQL(statement);
			}
		}

		onCreate(db);
	}

}