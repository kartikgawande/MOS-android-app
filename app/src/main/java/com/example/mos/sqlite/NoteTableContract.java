package com.example.mos.sqlite;

import android.provider.BaseColumns;

public class NoteTableContract {
    private NoteTableContract(){

    }

    public static class NoteTable implements BaseColumns{
        public static final String TABLE_NAME = "NoteTable";
        public static final String COLUMN_NAME_CATEGORY = "Category";
        public static final String COLUMN_NAME_CLASSIFICATION  = "Classification";
        public static final String COLUMN_NAME_CONTENT  = "Content";
        public static final String COLUMN_NAME_STATE  = "State";
    }

    public static class DailyLogsTable implements BaseColumns{
        public static final String TABLE_NAME = "DailyLogTable";
        public static final String COLUMN_DAY = "Day";
        public static final String COLUMN_DATE = "Date";
        public static final String COLUMN_MONTH = "Month";
        public static final String COLUMN_YEAR = "Year";
    }
}
