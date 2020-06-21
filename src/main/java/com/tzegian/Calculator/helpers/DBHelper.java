/*
    The database used for implementing history's database. Some basic functions.
*/

package com.tzegian.Calculator.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "history.db";
    public static final String TABLE = "history";
    private static final int DATABASE_VERSION = 1;
    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_CONTENT = "content";
    public static SQLiteDatabase database;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /* 
        Used when creating the database. 
    */
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_HISTORY = "CREATE TABLE " + TABLE
                + " ( "
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CONTENT + " TEXT NOT NULL);";

        // TODO Auto-generated method stub
        db.execSQL(CREATE_TABLE_HISTORY);
        database = db;
    }

    /* 
        Used when upgrading database. 
    */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    /* 
        Insert a new entry at history database 
    */
    public void insert(String history)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CONTENT, history);
        database.insert(TABLE, null, contentValues);
    }
}
