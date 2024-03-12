package com.example.mos.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SQLiteDbHelper extends SQLiteOpenHelper {
    Context context;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SQLiteDb.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NoteTableContract.NoteTable.TABLE_NAME + " (" +
                    NoteTableContract.NoteTable._ID + " INTEGER PRIMARY KEY," +
                    NoteTableContract.NoteTable.COLUMN_NAME_CATEGORY + " TEXT," +
                    NoteTableContract.NoteTable.COLUMN_NAME_CLASSIFICATION + " TEXT," +
                    NoteTableContract.NoteTable.COLUMN_NAME_CONTENT + " TEXT," +
                    NoteTableContract.NoteTable.COLUMN_NAME_STATE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NoteTableContract.NoteTable.TABLE_NAME;

    public SQLiteDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        this.context=context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void clearTable(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void addNote(SQLiteDatabase db, String classification, String category, String content, String state){
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

    public ArrayList<Map<String, String>> getAllRows(SQLiteDatabase db, String selection, String[] selectionArgs){
        ArrayList<Map<String, String>> result = new ArrayList<>();
        if(selection!=null)selection = selection + " = ?";

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
                selection,// The columns for the WHERE clause
                selectionArgs,// The values for the WHERE clause
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
                row.put(NoteTableContract.NoteTable.COLUMN_NAME_CATEGORY,category);
                String classification = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.NoteTable.COLUMN_NAME_CLASSIFICATION));
                row.put(NoteTableContract.NoteTable.COLUMN_NAME_CLASSIFICATION,classification);
                String content = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.NoteTable.COLUMN_NAME_CONTENT));
                row.put(NoteTableContract.NoteTable.COLUMN_NAME_CONTENT,content);
                String state = cursor.getString(cursor.getColumnIndexOrThrow(NoteTableContract.NoteTable.COLUMN_NAME_STATE));
                row.put(NoteTableContract.NoteTable.COLUMN_NAME_STATE,state);
                String id = cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID));
                row.put(BaseColumns._ID,id);

                result.add(row);
            }
        } finally {
            // Always close the cursor when you're done reading from it to free up resources.
            cursor.close();
        }
        return result;
    }

    public void updateRow(SQLiteDatabase db,String tableName,ContentValues values,String rowID){
        String selection = "_ID" + " = ?";
        String[] selectionArgs = {rowID};
        int count = db.update(
                tableName,   // the table to update in
                values,        // the values to update to
                selection,     // the column to select on
                selectionArgs  // the value to select on
        );
    }
}
