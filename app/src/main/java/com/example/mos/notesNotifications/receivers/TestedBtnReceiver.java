package com.example.mos.notesNotifications.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.BaseColumns;

import androidx.core.content.ContextCompat;

import com.example.mos.notesNotifications.services.DiscardBtnService;
import com.example.mos.notesNotifications.services.TestedBtnService;

import java.util.Objects;

public class TestedBtnReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, TestedBtnService.class);
        // Add extras or action to the intent if needed
        String notificationID = Objects.requireNonNull(intent.getStringExtra(BaseColumns._ID));
        serviceIntent.putExtra(BaseColumns._ID, notificationID);
        ContextCompat.startForegroundService(context, serviceIntent);
    }
}
