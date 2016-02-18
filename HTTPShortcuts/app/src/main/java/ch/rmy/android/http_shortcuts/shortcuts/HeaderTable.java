package ch.rmy.android.http_shortcuts.shortcuts;

public class HeaderTable {

    protected static final String TABLE_NAME = "headers";

    protected static final String COLUMN_ID = "_id";
    protected static final String COLUMN_SHORTCUT_ID = "shortcut_id";
    protected static final String COLUMN_KEY = "key";
    protected static final String COLUMN_VALUE = "value";

    /**
     * @return The sql statement to create the table
     */
    protected static String getCreateStatement() {
        return "create table if not exists " + TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_SHORTCUT_ID + " integer references "
                + ShortcutTable.TABLE_NAME + " on delete cascade, " + COLUMN_KEY + " text not null, " + COLUMN_VALUE + " text not null);";
    }

    /**
     * Returns the sql statements needed to upgrade the table from the preceding version.
     *
     * @param version The version to update to
     * @return The sql statements to update the table
     */
    protected static String[] getUpdateStatements(int version) {
        switch (version) {
            case 5:
                return new String[]{getCreateStatement()};
            default:
                return new String[]{};
        }
    }

}
