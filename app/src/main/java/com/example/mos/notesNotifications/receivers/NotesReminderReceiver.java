package com.example.mos.notesNotifications.receivers;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mos.CustomConstants;
import com.example.mos.R;
import com.example.mos.notesRV.NoteItemModel;
import com.example.mos.sqlite.DBUtils;
import com.example.mos.sqlite.SQLiteDbHelper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class NotesReminderReceiver extends BroadcastReceiver {

    private final String NOTES_NOTIFICATION_CHANNEL_ID = CustomConstants.NOTES_NOTIFICATION_CHANNEL_ID;
    private final int NOTES_NOTIFICATION_ID = CustomConstants.NOTES_NOTIFICATION_ID;

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.e(CustomConstants.CUSTOM_LOG_TAG, "notify");
        SQLiteDbHelper dbHelper = new SQLiteDbHelper(context);
        SQLiteDatabase dbr = dbHelper.getReadableDatabase();

        ArrayList<Map<String,String>> result = dbHelper.getAllRows(dbr, null, null);
        ArrayList<NoteItemModel> notes = DBUtils.convertResultToNotes(result);
        if(notes.isEmpty())return;
        Random rand = new Random();
        int randIndex = rand.nextInt(notes.size());
        String id = result.get(randIndex).get(BaseColumns._ID);

        Intent deleteIntent = new Intent(context, DiscardBtnReceiver.class);
        deleteIntent.putExtra(BaseColumns._ID,id);
        assert id != null;
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context,
                Integer.parseInt(id),
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent testedIntent = new Intent(context, TestedBtnReceiver.class);
        testedIntent.putExtra(BaseColumns._ID,id);
        assert id != null;
        PendingIntent TestedBtnPendingIntent = PendingIntent.getBroadcast(context,
                Integer.parseInt(id),
                testedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = notes.get(randIndex).getCategory() + ">" + notes.get(randIndex).getClassification();
        String content = notes.get(randIndex).getContent();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTES_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.mos_app_icon)
                .setContentTitle(title)
//                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.bin,"Discard",deletePendingIntent)
                .addAction(R.drawable.bin,"Tested",TestedBtnPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(Integer.parseInt(id), notificationBuilder.build());
            return;
        }
    }
}
