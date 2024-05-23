package com.example.mos.notesNotifications.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            // Reschedule the alarm here
            Intent NotesIntent = new Intent(context, NotesReminderReceiver.class);
            context.sendBroadcast(NotesIntent);
            Intent eyeCareIntent = new Intent(context, EyeCareReceiver.class);
            context.sendBroadcast(eyeCareIntent);
        }
    }
}
