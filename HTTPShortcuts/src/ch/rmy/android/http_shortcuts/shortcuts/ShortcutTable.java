package ch.rmy.android.http_shortcuts.shortcuts;

/**
 * Defines the structure of the database table
 * 
 * @author Roland Meyer
 */
public class ShortcutTable {

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
	protected static final String COLUMN_POSITION = "position";
	protected static final String COLUMN_UNUSED = "unused";
	protected static final String COLUMN_DESCRIPTION = "description";
	protected static final String COLUMN_BODY_CONTENT = "body_content";
	protected static final String COLUMN_TIMEOUT = "timeout";

	/**
	 * @return The sql statement to create the table
	 */
	protected static String getCreateStatement() {
		return "create table if not exists " + TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_NAME + " text, " + COLUMN_PROTOCOL + " text, "
				+ COLUMN_URL + " text, " + COLUMN_METHOD + " text, " + COLUMN_USERNAME + " text, " + COLUMN_PASSWORD + " text, " + COLUMN_ICON + " text, " + COLUMN_FEEDBACK
				+ " integer, " + COLUMN_UNUSED + " text, " + COLUMN_POSITION + " integer not null default 0, " + COLUMN_DESCRIPTION + " text, " + COLUMN_BODY_CONTENT + " text, "
				+ COLUMN_TIMEOUT + " integer not null default 0);";
	}

	/**
	 * Returns the sql statements needed to upgrade the table from the preceding version.
	 * 
	 * @param version
	 *            The version to update to
	 * @return The sql statements to update the table
	 */
	protected static String[] getUpdateStatements(int version) {
		switch (version) {
		case 2:
			return new String[] { "alter table " + TABLE_NAME + " ADD COLUMN " + COLUMN_POSITION + " integer not null default 0;",
					"update " + TABLE_NAME + " set " + COLUMN_POSITION + " = " + COLUMN_ID + ";" };
		case 3:
			return new String[] { "alter table " + TABLE_NAME + " ADD COLUMN " + COLUMN_DESCRIPTION + " text;" };
		case 6:
			return new String[] { "alter table " + TABLE_NAME + " ADD COLUMN " + COLUMN_BODY_CONTENT + " text;" };
		case 7:
			return new String[] { "alter table " + TABLE_NAME + " ADD COLUMN " + COLUMN_TIMEOUT + " integer not null default 0;" };
		default:
			return new String[] {};
		}
	}

}
