package com.example.mos.notesNotifications.receivers;

import static com.example.mos.CustomConstants.DISCARDED_STATE;
import static com.example.mos.CustomConstants.EXPERIMENTAL_STATE;
import static com.example.mos.CustomConstants.MAIN_ACTIVITY;
import static com.example.mos.CustomConstants.SOURCE;
import static com.example.mos.CustomConstants.TESTED_STATE;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mos.ui.notes.AddEditNoteActivity;
import com.example.mos.CustomConstants;
import com.example.mos.R;
import com.example.mos.ui.notes.notesRV.NoteItemModel;
import com.example.mos.sqlite.SQLiteDbQueries;

import java.util.ArrayList;
import java.util.Random;

public class NotesReminderReceiver extends BroadcastReceiver {

    private final String NOTES_NOTIFICATION_CHANNEL_ID = CustomConstants.NOTES_NOTIFICATION_CHANNEL_ID;
    private final int NOTES_NOTIFICATION_ID = CustomConstants.NOTES_NOTIFICATION_ID;

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.e(CustomConstants.CUSTOM_LOG_TAG, "notify");
        SQLiteDbQueries dbHelper = new SQLiteDbQueries(context);
        SQLiteDatabase dbr = dbHelper.getReadableDatabase();

        ArrayList<NoteItemModel> notes = dbHelper.getAllNotes(dbr);
        if(notes.isEmpty())return;
        boolean haveNonDiscarded = false;
        for(NoteItemModel note: notes){
            if(note.getState().equals(TESTED_STATE) || note.getState().equals(EXPERIMENTAL_STATE)) haveNonDiscarded = true;
        }
        if(!haveNonDiscarded) return;
        Random rand = new Random();
        int randIndex;
        while(true) {
            randIndex = rand.nextInt(notes.size());
            String state = notes.get(randIndex).getState();
            if(!state.equals(DISCARDED_STATE)) break;
        }
        String id = notes.get(randIndex).getDbRowNo();

//        Intent deleteIntent = new Intent(context, DiscardBtnReceiver.class);
//        deleteIntent.putExtra(BaseColumns._ID,id);
//        assert id != null;
//        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context,
//                Integer.parseInt(id),
//                deleteIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        Intent testedIntent = new Intent(context, TestedBtnReceiver.class);
//        testedIntent.putExtra(BaseColumns._ID,id);
//        assert id != null;
//        PendingIntent TestedBtnPendingIntent = PendingIntent.getBroadcast(context,
//                Integer.parseInt(id),
//                testedIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NoteItemModel note = notes.get(randIndex);
        Intent editNoteIntent = new Intent(context, AddEditNoteActivity.class);
        editNoteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        editNoteIntent.putExtra(CustomConstants.CALLED_BY,CustomConstants.NOTES_RV_ADAPTER);
        editNoteIntent.putExtra(CustomConstants.NOTE_CATEGORY,note.getCategory());
        editNoteIntent.putExtra(CustomConstants.NOTE_CLASSIFICATION,note.getClassification());
        editNoteIntent.putExtra(CustomConstants.NOTE_STATE,note.getState());
        editNoteIntent.putExtra(CustomConstants.NOTE_CONTENT,note.getContent());
        editNoteIntent.putExtra(CustomConstants.NOTE_ID,note.getDbRowNo());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, editNoteIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String title = notes.get(randIndex).getCategory() + ">" + notes.get(randIndex).getClassification()+" ("+
                notes.get(randIndex).getState()+")";
        String content = notes.get(randIndex).getContent();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTES_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.mos_app_icon)
                .setContentTitle(title)
//                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
//                .addAction(R.drawable.bin,"Discard",deletePendingIntent)
//                .addAction(R.drawable.bin,"Tested",TestedBtnPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            Intent alarmScheduleIntent = new Intent(context, EyeCareReceiver.class);
            if(PendingIntent.getBroadcast(context, 0, alarmScheduleIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) == null){
                //not scheduled
                PendingIntent alarmSchedulePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                scheduleNextAlarm(context, alarmSchedulePendingIntent);
            }

            String calledBy = intent.getStringExtra(SOURCE);
            if(calledBy!=null && calledBy.equals(MAIN_ACTIVITY)) return;

//            notificationManager.cancelAll();
            notificationManager.notify(Integer.parseInt(id), notificationBuilder.build());
        }
    }

    private void scheduleNextAlarm(Context context, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.HOUR, 1); // Schedule for one hour later again

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            // Fall back for older APIs, though less relevant since you need Android M or higher for setExactAndAllowWhileIdle.
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
