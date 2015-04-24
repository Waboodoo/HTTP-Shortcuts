package ch.rmy.android.http_shortcuts.shortcuts;

public class Table {

	protected static final String TABLE_NAME = "shortcuts";

	protected static final String COLUMN_ID = "_id";
	protected static final String COLUMN_NAME = "name";
	protected static final String COLUMN_PROTOCOL = "protocol";
	protected static final String COLUMN_URL = "url";
	protected static final String COLUMN_METHOD = "method";
	protected static final String COLUMN_USERNAME = "username";
	protected static final String COLUMN_PASSWORD = "password";
	protected static final String COLUMN_ICON = "icon";
	protected static final String COLUMN_FEEDBACK = "feedback";
	protected static final String COLUMN_UNUSED = "unused";

	protected static String getCreateStatement() {
		return "create table " + TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_NAME + " text, " + COLUMN_PROTOCOL + " text, " + COLUMN_URL
				+ " text, " + COLUMN_METHOD + " text, " + COLUMN_USERNAME + " text, " + COLUMN_PASSWORD + " text, " + COLUMN_ICON + " text, " + COLUMN_FEEDBACK + " integer, "
				+ COLUMN_UNUSED + " text);";
	}

	protected static String getDropStatement() {
		return "drop table if exists " + TABLE_NAME;
	}

}
