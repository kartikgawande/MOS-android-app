package com.example.mos.ui.dailyLogs.dailyLogRV;

public class DailyLogModel {
    String day;
    String date;
    String month;
    String year;

    public String getDbRowNo() {
        return dbRowNo;
    }

    public void setDbRowNo(String dbRowNo) {
        this.dbRowNo = dbRowNo;
    }

    String dbRowNo;

    public DailyLogModel(String day, String date, String month, String year, String dbRowNo) {
        this.day = day;
        this.date = date;
        this.month = month;
        this.year = year;
        this.dbRowNo=dbRowNo;
    }

    public DailyLogModel() {
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
