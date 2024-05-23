package com.example.mos;

import static com.example.mos.CustomConstants.ALL_CATEGORIES;
import static com.example.mos.CustomConstants.ALL_CLASSES;
import static com.example.mos.CustomConstants.ALL_STATES;
import static com.example.mos.CustomConstants.DISCARDED_STATE;
import static com.example.mos.CustomConstants.EMOTION_CLASSIFICATION;
import static com.example.mos.CustomConstants.EXPERIMENTAL_STATE;
import static com.example.mos.CustomConstants.FINANCIAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.MENTAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.MINDSET_CATEGORY;
import static com.example.mos.CustomConstants.PHYSICAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.RULES_CATEGORY;
import static com.example.mos.CustomConstants.SOCIAL_CLASSIFICATION;
import static com.example.mos.CustomConstants.TESTED_STATE;
import static com.example.mos.CustomConstants.TYPE_CATEGORY;
import static com.example.mos.CustomConstants.TYPE_CLASS;
import static com.example.mos.CustomConstants.TYPE_STATE;

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
import android.os.Build;
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
import android.widget.Button;
import android.widget.Toast;

import com.example.mos.googleDrive.GoogleDriveUtils;
import com.example.mos.notesNotifications.receivers.EyeCareReceiver;
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
import java.util.Iterator;
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

    private void createNotificationChannels() {
        CharSequence channelName = "Notes Notifications";
        String description = "Channel for Revising Notes";
        int channelImportance = NotificationManager.IMPORTANCE_DEFAULT;
        createNotificationChannel(channelName, description, channelImportance, CustomConstants.NOTES_NOTIFICATION_CHANNEL_ID);

        CharSequence serviceChannelName = "Service Notifications";
        String serviceDescription = "Channel about notifying foreground services.";
        int serviceChannelImportance = NotificationManager.IMPORTANCE_DEFAULT;
        createNotificationChannel(serviceChannelName, serviceDescription,serviceChannelImportance, CustomConstants.SERVICE_NOTIFICATION_CHANNEL_ID);

        CharSequence eyeCareChannelName = "Eye Care Notifications";
        String eyeCareDescription = "Channel about notifying to see far every 20 minutes.";
        int eyeCareChannelImportance = NotificationManager.IMPORTANCE_HIGH;
        createNotificationChannel(eyeCareChannelName, eyeCareDescription, eyeCareChannelImportance, CustomConstants.EYE_CARE_NOTIFICATION_CHANNEL_ID);
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

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // You cannot post notifications. Ask for permission.
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    CustomConstants.NOTES_NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }

        createNotificationChannels();
//        scheduleAlarm(NOTIFICATION_REPEAT_AFTER_MILIS);


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

        Button classFilterBtn = findViewById(R.id.classFilterBtn);
        Button categoryFilterBtn = findViewById(R.id.categoryFilterBtn);
        Button stateFilterBtn = findViewById(R.id.stateFilterBtn);

        classFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = classFilterBtn.getText().toString();
                if(label.equals(ALL_CLASSES)) classFilterBtn.setText(PHYSICAL_CLASSIFICATION);
                else if(label.equals(PHYSICAL_CLASSIFICATION)) classFilterBtn.setText(SOCIAL_CLASSIFICATION);
                else if(label.equals(SOCIAL_CLASSIFICATION)) classFilterBtn.setText(MENTAL_CLASSIFICATION);
                else if(label.equals(MENTAL_CLASSIFICATION)) classFilterBtn.setText(EMOTION_CLASSIFICATION);
                else if(label.equals(EMOTION_CLASSIFICATION)) classFilterBtn.setText(FINANCIAL_CLASSIFICATION);
                else if(label.equals(FINANCIAL_CLASSIFICATION)) classFilterBtn.setText(ALL_CLASSES);

                filterRVnotes(classFilterBtn, categoryFilterBtn, stateFilterBtn);
            }
        });

        categoryFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = categoryFilterBtn.getText().toString();
                if(label.equals(ALL_CATEGORIES)) categoryFilterBtn.setText(MINDSET_CATEGORY);
                else if(label.equals(MINDSET_CATEGORY)) categoryFilterBtn.setText(RULES_CATEGORY);
                else if(label.equals(RULES_CATEGORY)) categoryFilterBtn.setText(ALL_CATEGORIES);

                filterRVnotes(classFilterBtn, categoryFilterBtn, stateFilterBtn);
            }
        });

        stateFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = stateFilterBtn.getText().toString();
                if(label.equals(ALL_STATES)) stateFilterBtn.setText(EXPERIMENTAL_STATE);
                else if(label.equals(EXPERIMENTAL_STATE)) stateFilterBtn.setText(TESTED_STATE);
                else if(label.equals(TESTED_STATE)) stateFilterBtn.setText(DISCARDED_STATE);
                else if(label.equals(DISCARDED_STATE)) stateFilterBtn.setText(ALL_STATES);

                filterRVnotes(classFilterBtn, categoryFilterBtn, stateFilterBtn);
            }
        });
    }

    private void filterRVnotes(Button classFilterBtn, Button categoryFilterBtn, Button stateFilterBtn){
        String classFilter = classFilterBtn.getText().toString();
        String categoryFilter = categoryFilterBtn.getText().toString();
        String stateFilter = stateFilterBtn.getText().toString();

        notes.clear();
        notes.addAll(filterNotes(classFilter,categoryFilter,stateFilter));
        Collections.reverse(notes);
        notesRVadapter.notifyDataSetChanged();
    }

    private ArrayList<NoteItemModel> refreshNotesArraylistFromLocalDB() {
        dbHelper = new SQLiteDbHelper(MainActivity.this);
        SQLiteDatabase dbw = dbHelper.getWritableDatabase();
        SQLiteDatabase dbr = dbHelper.getReadableDatabase();
        ArrayList<Map<String,String>> result = dbHelper.getAllRows(dbr, null, null);
        ArrayList<NoteItemModel> notes = new ArrayList<>(DBUtils.convertResultToNotes(result));
        return notes;
    }

    private ArrayList<NoteItemModel> filterNotes(String CLASS ,String CATEGORY, String STATE){
        ArrayList<NoteItemModel> notes = new ArrayList<>();
        notes.addAll(refreshNotesArraylistFromLocalDB());
        Iterator<NoteItemModel> noteIterator = notes.iterator();
        if(!CLASS.equals(ALL_CLASSES)) {
            while(noteIterator.hasNext()){
                NoteItemModel note = noteIterator.next();
                if (!note.getClassification().equals(CLASS)) noteIterator.remove();
            }
        }
        noteIterator = notes.iterator();
        if(!CATEGORY.equals(ALL_CATEGORIES)) {
            while(noteIterator.hasNext()){
                NoteItemModel note = noteIterator.next();
                if (!note.getCategory().equals(CATEGORY)) noteIterator.remove();
            }
        }
        noteIterator = notes.iterator();
        if(!STATE.equals(ALL_STATES)){
            while(noteIterator.hasNext()){
                NoteItemModel note = noteIterator.next();
                if (!note.getState().equals(STATE)) noteIterator.remove();
            }
        }
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

    private void createNotificationChannel(CharSequence name, String description, int importance, String channelId) {
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        //register receiver
//        IntentFilter intentFilter = new IntentFilter("com.example.mos.notifications.NOTIFICATIONEVENT");
//        NotesReminderReceiver receiver = new NotesReminderReceiver();
//        registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED);
    }

//    private void scheduleAlarm(int interval) {
//        Intent intent = new Intent(MainActivity.this, NotesReminderReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//
//        // Set the alarm to start at approximately the current time.
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//
//        // Use 60000 milliseconds as the interval for a 1-minute repeat.
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                interval, pendingIntent);
//
//        alarmManager.canScheduleExactAlarms();
//
//        //20 20 20 eye rule
//        Intent intent202020 = new Intent(MainActivity.this, NotesReminderReceiver.class);
//        PendingIntent pendingIntent202020 = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//
//        AlarmManager alarmManager202020 = (AlarmManager) getSystemService(ALARM_SERVICE);
//
//        // Set the alarm to start at approximately the current time.
//        Calendar calendar202020 = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//
//        // Use 60000 milliseconds as the interval for a 1-minute repeat.
////        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar202020.getTimeInMillis(),
////                interval, pendingIntent);
//
//        // Schedule the alarm
//        calendar.add(Calendar.SECOND, 10);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//        } else {
//            // Fall back for older APIs, though less relevant since you need Android M or higher for setExactAndAllowWhileIdle.
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//        }
//    }

    public void showStartNotification() {
        Intent NotesIntent = new Intent(this, NotesReminderReceiver.class);
        sendBroadcast(NotesIntent);
        Intent eyeCareIntent = new Intent(this, EyeCareReceiver.class);
        sendBroadcast(eyeCareIntent);
    }
}