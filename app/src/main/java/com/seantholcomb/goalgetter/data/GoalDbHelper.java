package com.seantholcomb.goalgetter.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.seantholcomb.goalgetter.data.GoalContract.GoalEntry;

/**
 * Created by seanholcomb on 10/8/15.
 */
public class GoalDbHelper extends SQLiteOpenHelper{

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "goalgetter.db";

    public GoalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_GOAL_TABLE = "CREATE TABLE " + GoalEntry.TABLE_NAME + " (" +
                GoalEntry._ID + " INTEGER PRIMARY KEY, " +
                GoalEntry.COLUMN_ID + " TEXT  NOT NULL, " +
                GoalEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                GoalEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                GoalEntry.COLUMN_START_DATE+ " REAL NOT NULL, " +
                GoalEntry.COLUMN_DUE_DATE + " REAL NOT NULL, " +
                GoalEntry.COLUMN_TASK + " TEXT NOT NULL, " +
                GoalEntry.COLUMN_FREQUENCY + " INTEGER NOT NULL, " +
                GoalEntry.COLUMN_TOTAL_TASKS + " INTEGER NOT NULL, " +
                GoalEntry.COLUMN_TASKS_DONE + " INTEGER NOT NULL, " +
                GoalEntry.COLUMN_TASKS_MISSED + " INTEGER NOT NULL, " +
                GoalEntry.COLUMN_TASKS_REMAINING + " INTEGER NOT NULL, " +
                GoalEntry.COLUMN_STATUS + " TEXT NOT NULL " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_GOAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        onCreate(sqLiteDatabase);
    }
}