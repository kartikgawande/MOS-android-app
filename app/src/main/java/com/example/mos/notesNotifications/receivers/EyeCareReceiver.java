package com.example.mos.notesNotifications.receivers;

import static com.example.mos.CustomConstants.EYE_CARE_NOTIFICATION_CHANNEL_ID;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mos.R;

public class EyeCareReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, EYE_CARE_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.mos_app_icon)
                .setContentTitle("Eye Care")
                .setContentText("Look away for 20 seconds!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setTimeoutAfter(20000); // Set the notification to disappear after 20 seconds (20000 milliseconds)

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            Intent alarmScheduleIntent = new Intent(context, EyeCareReceiver.class);
            if(PendingIntent.getBroadcast(context, 0, alarmScheduleIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null){
//                Toast.makeText(context, "Already scheduled.", Toast.LENGTH_SHORT).show();
                return;
            }
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            scheduleNextAlarm(context, pendingIntent);

//            notificationManager.cancelAll();
            notificationManager.notify(0, notificationBuilder.build());
            // Schedule sound to play after 20 seconds
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playSound(context);
                }
            }, 20000); // Delay matches the notification timeout
        }
    }

    private void playSound(Context context) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.eye_care_notification_disappear);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        }
    }

    private void scheduleNextAlarm(Context context, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(!alarmManager.canScheduleExactAlarms()){
                Toast.makeText(context, "Cant schedule alarms.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 20); // Schedule for 20 minute later again

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            // Fall back for older APIs, though less relevant since you need Android M or higher for setExactAndAllowWhileIdle.
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
