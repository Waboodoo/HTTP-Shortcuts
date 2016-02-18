package ch.rmy.android.http_shortcuts.shortcuts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public class DBCreator extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "shortcuts.db";

    public DBCreator(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ShortcutTable.getCreateStatement());
        db.execSQL(PostParameterTable.getCreateStatement());
        db.execSQL(HeaderTable.getCreateStatement());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int version = oldVersion + 1; version <= newVersion; version++) {
            for (String statement : ShortcutTable.getUpdateStatements(version)) {
                db.execSQL(statement);
            }
            for (String statement : PostParameterTable.getUpdateStatements(version)) {
                db.execSQL(statement);
            }
            for (String statement : HeaderTable.getUpdateStatements(version)) {
                db.execSQL(statement);
            }
        }

        onCreate(db);
    }

    @SuppressLint("NewApi")
    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        }
        return db;
    }

    @SuppressLint("NewApi")
    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = super.getReadableDatabase();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        }
        return db;
    }

}