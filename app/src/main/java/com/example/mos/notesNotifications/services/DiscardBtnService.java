package com.example.mos.notesNotifications.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mos.CustomConstants;
import com.example.mos.R;
import com.example.mos.googleDrive.GoogleDriveUtils;
import com.example.mos.sqlite.DBUtils;
import com.example.mos.sqlite.SQLiteDbHelper;
import com.google.api.services.drive.Drive;

import java.util.Objects;

public class DiscardBtnService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String notificationID = Objects.requireNonNull(intent.getStringExtra(BaseColumns._ID));
        SQLiteDbHelper dbHelper = new SQLiteDbHelper(getApplicationContext());

        SQLiteDatabase dbw = dbHelper.getWritableDatabase();
        DBUtils.deleteRow(notificationID,dbw);
        dbw.close();

        Context context = getApplicationContext();
        Drive googleDriveService = GoogleDriveUtils.getDriveServiceFromLastSignedIn(context);
        DBUtils.backupDBtoDrive(getApplicationContext(), SQLiteDbHelper.DATABASE_NAME, googleDriveService);

        Notification notification = new NotificationCompat.Builder(this, CustomConstants.SERVICE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Service Running")
                .setContentText("Uploaded changes.")
                .setSmallIcon(R.drawable.mos_app_icon) // Ensure you have this drawable in your resources
                // Add more customization to the notification as needed
                .build();
        // Call startForeground with a unique ID and the built notification
        startForeground(1, notification);
        return START_STICKY;
    }
}
