package com.example.mos;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.widget.Toast;

import com.example.mos.googleDrive.GoogleDriveUtils;
import com.example.mos.notesRV.NoteItemModel;
import com.example.mos.notesRV.NotesRVadapter;
import com.example.mos.notesNotifications.receivers.NotesReminderReceiver;
import com.example.mos.sqlite.DBUtils;
import com.example.mos.sqlite.SQLiteDbHelper;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.services.drive.Drive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleDriveUtils.OnDriveUpdatedLocalDBlistener {

    SQLiteDbHelper dbHelper;
    String dbName = SQLiteDbHelper.DATABASE_NAME;
    String CUSTOM_LOG_TAG = CustomConstants.CUSTOM_LOG_TAG;
    String APP_DRIVE_DIR = CustomConstants.DRIVE_APP_DIR;
    NotesRVadapter notesRVadapter;
    RecyclerView noteRV;
    Intent signInIntent;
    ActivityResultLauncher<Intent> signInLauncher;
    public static Boolean notesRVupdating=false;

    private static int NOTIFICATION_REPEAT_AFTER_MILIS=1000*60*60;

    ArrayList<NoteItemModel> notes = new ArrayList<>();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==CustomConstants.NOTES_NOTIFICATION_PERMISSION_REQUEST_CODE && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            //notes notification permission granted
            createNotificationChannels();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notesRVlayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new GoogleDriveUtils(this); //this is important dont delete

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // You can post notifications.
            createNotificationChannels();
        } else {
            // You cannot post notifications. Ask for permission.
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    CustomConstants.NOTES_NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }

        createNotificationChannels();
        scheduleAlarm(NOTIFICATION_REPEAT_AFTER_MILIS);


        noteRV = findViewById(R.id.notesRV);
        notesRVadapter = new NotesRVadapter(this, notes);
        noteRV.setLayoutManager(new LinearLayoutManager(this));
        noteRV.setAdapter(notesRVadapter);

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intentData = result.getData();
                        try {
                            Drive googleDriveService = GoogleDriveUtils.getDriveService(MainActivity.this,intentData);
                            notesRVupdating=true;
                            GoogleDriveUtils.downloadFileSaveAndUpdateDBlocalFile(dbName,
                                    googleDriveService,
                                    dbName,APP_DRIVE_DIR,
                                    MainActivity.this,
                                    dbName,
                                    dbName);

                        } catch (Exception e) {
                            // Google Sign In failed
                            Log.w(CUSTOM_LOG_TAG, "", e);
                        }
                    }
                }
        );

        GoogleSignInClient client = GoogleDriveUtils.signInGoogleAndGetClient(MainActivity.this);
        signInIntent = client.getSignInIntent();
        signInLauncher.launch(signInIntent);

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addNoteActIntent = new Intent(MainActivity.this, AddNoteActivity.class);
                notesRVupdating=false;
                startActivity(addNoteActIntent);
            }
        });
    }

    private ArrayList<NoteItemModel> refreshNotesArraylistFromLocalDB() {
        dbHelper = new SQLiteDbHelper(MainActivity.this);
        SQLiteDatabase dbw = dbHelper.getWritableDatabase();
        SQLiteDatabase dbr = dbHelper.getReadableDatabase();
        ArrayList<Map<String,String>> result = dbHelper.getAllRows(dbr, null, null);
        ArrayList<NoteItemModel> notes = new ArrayList<>(DBUtils.convertResultToNotes(result));
        return notes;
    }

    @Override
    public void setOnDriveUpdatedLocalDBListener() {
        notes.clear();
        notes.addAll(refreshNotesArraylistFromLocalDB());
        Collections.reverse(notes);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notesRVadapter.notifyDataSetChanged();
                showStartNotification();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!notesRVupdating)signInLauncher.launch(signInIntent);
    }

    public void customOnResume(){
        while (notesRVupdating);
        if(!notesRVupdating)signInLauncher.launch(signInIntent);
    }

    private void createNotificationChannels() {
        CharSequence channelName = "Notes Notifications";
        String description = "Channel for Reminding Notes";
        int channelImportance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CustomConstants.NOTES_NOTIFICATION_CHANNEL_ID, channelName, channelImportance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        //register receiver
        IntentFilter intentFilter = new IntentFilter("com.example.mos.notifications.NOTIFICATIONEVENT");
        NotesReminderReceiver receiver = new NotesReminderReceiver();
        registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED);

        CharSequence channelName1 = "Service Notifications";
        String description1 = "Channel about notifying foreground services.";
        int channelImportance1 = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel1 = new NotificationChannel(CustomConstants.SERVICE_NOTIFICATION_CHANNEL_ID, channelName1, channelImportance1);
        channel1.setDescription(description1);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager1 = getSystemService(NotificationManager.class);
        notificationManager1.createNotificationChannel(channel1);
    }

    private void scheduleAlarm(int interval) {
        Intent intent = new Intent(MainActivity.this, NotesReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Set the alarm to start at approximately the current time.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Use 60000 milliseconds as the interval for a 1-minute repeat.
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                interval, pendingIntent);

        //20 20 20 eye rule
        Intent intent202020 = new Intent(MainActivity.this, NotesReminderReceiver.class);
        PendingIntent pendingIntent202020 = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager202020 = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Set the alarm to start at approximately the current time.
        Calendar calendar202020 = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Use 60000 milliseconds as the interval for a 1-minute repeat.
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar202020.getTimeInMillis(),
                interval, pendingIntent);
    }

    public void showStartNotification() {
        Intent intent = new Intent(this, NotesReminderReceiver.class);
        sendBroadcast(intent);
    }
}