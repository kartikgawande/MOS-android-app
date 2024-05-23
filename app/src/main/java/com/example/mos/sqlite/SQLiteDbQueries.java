package com.example.mos.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.mos.CustomConstants;
import com.example.mos.ui.dailyLogs.dailyLogRV.DailyLogModel;
import com.example.mos.ui.notes.notesRV.NoteItemModel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SQLiteDbQueries extends SQLiteOpenHelper {
    Context context;
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "SQLiteDb.db";

    private static final String SQL_CREATE_NOTE_TABLE =
            "CREATE TABLE " + NoteTableContract.NoteTable.TABLE_NAME + " (" +
                    NoteTableContract.NoteTable._ID + " INTEGER PRIMARY KEY," +
                    NoteTableContract.NoteTable.COLUMN_NAME_CATEGORY + " TEXT," +
                    NoteTableContract.NoteTable.COLUMN_NAME_CLASSIFICATION + " TEXT," +
                    NoteTableContract.NoteTable.COLUMN_NAME_CONTENT + " TEXT," +
                    NoteTableContract.NoteTable.COLUMN_NAME_STATE + " TEXT)";
    private static final String SQL_CREATE_DAILY_LOG_TABLE =
            "CREATE TABLE " + NoteTableContract.DailyLogsTable.TABLE_NAME + " (" +
                    NoteTableContract.DailyLogsTable._ID + " INTEGER PRIMARY KEY," +
                    NoteTableContract.DailyLogsTable.COLUMN_DAY + " TEXT," +
                    NoteTableContract.DailyLogsTable.COLUMN_DATE + " TEXT," +
                    NoteTableContract.DailyLogsTable.COLUMN_MONTH + " TEXT," +
                    NoteTableContract.DailyLogsTable.COLUMN_YEAR + " TEXT)";

    public SQLiteDbQueries(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        this.context=context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NOTE_TABLE);
        Log.e(CustomConstants.CUSTOM_LOG_TAG, "Created not table ");
        db.execSQL(SQL_CREATE_DAILY_LOG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(deleteTableCmd(NoteTableContract.NoteTable.TABLE_NAME));
        db.execSQL(deleteTableCmd(NoteTableContract.DailyLogsTable.TABLE_NAME));
        onCreate(db);
    }

    private String deleteTableCmd(String tableName){
        return  "DROP TABLE IF EXISTS " + tableName;
    }

    public void addNote (SQLiteDatabase db, String classification, String category, String
    content, String state){
        // Gets the data repository in write mode 'db'
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(NoteTableContract.NoteTable.COLUMN_NAME_CATEGORY, category);
        values.put(NoteTableContract.NoteTable.COLUMN_NAME_CLASSIFICATION, classification);
        values.put(NoteTableContract.NoteTable.COLUMN_NAME_CONTENT, content);
        values.put(NoteTableContract.NoteTable.COLUMN_NAME_STATE, state);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(NoteTableContract.NoteTable.TABLE_NAME, null, values);
    }

    public ArrayList<NoteItemModel> getAllNotes (SQLiteDatabase db){
        ArrayList<NoteItemModel> notes = new ArrayList<>();

        String[] projection = {
                BaseColumns._ID,
                NoteTableContract.NoteTable.COLUMN_NAME_CATEGORY,
                NoteTableContract.NoteTable.COLUMN_NAME_CLASSIFICATION,
                NoteTableContract.NoteTable.COLUMN_NAME_CONTENT,
                NoteTableContract.NoteTable.COLUMN_NAME_STATE
        };

//        String sortOrder = NoteTableContract.NoteTable.COLUMN_NAME_STATE + " DESC";

        Cursor cursor = db.query(
                NoteTableContract.NoteTable.TABLE_NAME,// The table to query
                projection,// The columns to return
                null,// The columns for the WHERE clause
                null,// The values for the WHERE clause
                null,// don't group the rows
                null,// don't filter by row groups
                null// The sort order
        );

//        Toast.makeText(context,String.valueOf(cursor.getCount()), Toast.LENGTH_SHORT).show();

        try {
            // Iterate over the rows in the cursor
            while (cursor.moveToNext()) {
                Map<String, String> row = new HashMap<>();
                // Extract data.
                String category = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.NoteTable.COLUMN_NAME_CATEGORY));
//                row.put(NoteTableContract.NoteTable.COLUMN_NAME_CATEGORY, category);
                String classification = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.NoteTable.COLUMN_NAME_CLASSIFICATION));
//                row.put(NoteTableContract.NoteTable.COLUMN_NAME_CLASSIFICATION, classification);
                String content = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.NoteTable.COLUMN_NAME_CONTENT));
//                row.put(NoteTableContract.NoteTable.COLUMN_NAME_CONTENT, content);
                String state = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.NoteTable.COLUMN_NAME_STATE));
//                row.put(NoteTableContract.NoteTable.COLUMN_NAME_STATE, state);
                String id = cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID));
//                row.put(BaseColumns._ID, id);


                NoteItemModel note = new NoteItemModel();
                note.setState(state);
                note.setCategory(category);
                note.setContent(content);
                note.setClassification(classification);
                note.setDbRowNo(id);

                notes.add(note);

//                result.add(row);
            }
        } finally {
            // Always close the cursor when you're done reading from it to free up resources.
            cursor.close();
        }
//        return result;
        return notes;
    }

    public void updateRow (SQLiteDatabase db, String tableName, ContentValues values, String
    rowID){
        String selection = "_ID" + " = ?";
        String[] selectionArgs = {rowID};
        int count = db.update(
                tableName,   // the table to update in
                values,        // the values to update to
                selection,     // the column to select on
                selectionArgs  // the value to select on
        );
    }

    public static void deleteRow (String notificationID, SQLiteDatabase dbw){
        dbw.delete(NoteTableContract.NoteTable.TABLE_NAME, "_ID = ?", new String[]{notificationID});
    }

    public void addDailyLog(SQLiteDatabase db,String day, String date, String month, String year){
        // Gets the data repository in write mode 'db'
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(NoteTableContract.DailyLogsTable.COLUMN_DAY, day);
        values.put(NoteTableContract.DailyLogsTable.COLUMN_DATE, date);
        values.put(NoteTableContract.DailyLogsTable.COLUMN_MONTH, month);
        values.put(NoteTableContract.DailyLogsTable.COLUMN_YEAR, year);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(NoteTableContract.DailyLogsTable.TABLE_NAME, null, values);
    }


    public ArrayList<DailyLogModel> getAllDailyLogs (SQLiteDatabase db){
        ArrayList<DailyLogModel> dailyLogs = new ArrayList<>();

        String[] projection = {
                BaseColumns._ID,
                NoteTableContract.DailyLogsTable.COLUMN_DAY,
                NoteTableContract.DailyLogsTable.COLUMN_DATE,
                NoteTableContract.DailyLogsTable.COLUMN_MONTH,
                NoteTableContract.DailyLogsTable.COLUMN_YEAR
        };

//        String sortOrder = NoteTableContract.NoteTable.COLUMN_NAME_STATE + " DESC";

        Cursor cursor = db.query(
                NoteTableContract.DailyLogsTable.TABLE_NAME,// The table to query
                projection,// The columns to return
                null,// The columns for the WHERE clause
                null,// The values for the WHERE clause
                null,// don't group the rows
                null,// don't filter by row groups
                null// The sort order
        );

//        Toast.makeText(context,String.valueOf(cursor.getCount()), Toast.LENGTH_SHORT).show();

        try {
            // Iterate over the rows in the cursor
            while (cursor.moveToNext()) {
                Map<String, String> row = new HashMap<>();
                // Extract data.
                String day = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.DailyLogsTable.COLUMN_DAY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.DailyLogsTable.COLUMN_DATE));
                String month = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.DailyLogsTable.COLUMN_MONTH));
                String year = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.DailyLogsTable.COLUMN_YEAR));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID));


                DailyLogModel dailyLog = new DailyLogModel();
                dailyLog.setDay(day);
                dailyLog.setDate(date);
                dailyLog.setMonth(month);
                dailyLog.setYear(year);
                dailyLog.setDbRowNo(id);

                dailyLogs.add(dailyLog);
            }
        } finally {
            // Always close the cursor when you're done reading from it to free up resources.
            cursor.close();
        }
//        return result;
        return dailyLogs;
    }
}
