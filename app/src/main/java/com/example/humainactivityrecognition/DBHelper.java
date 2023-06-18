package com.example.humainactivityrecognition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "activity_db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "activity_logs";
    private static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_ACTIVITY = "activity";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_ACTIVITY + " TEXT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_TIME + " TEXT)";

        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the table if it exists and recreate it
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertActivityLog(String email, String activity, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_ACTIVITY, activity);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public Cursor retrieveActivityLogs(String userEmail) {
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {COLUMN_ACTIVITY, COLUMN_DATE, COLUMN_TIME};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {userEmail};

        return db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
    }

}

