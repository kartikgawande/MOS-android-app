package com.example.mos;

import static com.example.mos.CustomConstants.MAIN_ACTIVITY;
import static com.example.mos.CustomConstants.NOTES_ACTIVITY;
import static com.example.mos.CustomConstants.SOURCE;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.example.mos.googleDrive.GoogleDriveUtils;
import com.example.mos.notesNotifications.receivers.EyeCareReceiver;
import com.example.mos.notesNotifications.receivers.NotesReminderReceiver;
import com.example.mos.sqlite.SQLiteDbQueries;
import com.example.mos.ui.SharedViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mos.databinding.ActivityMainBinding;
import com.google.api.services.drive.Drive;

public class MainActivity extends AppCompatActivity implements GoogleDriveUtils.OnDriveUpdatedLocalDBlistener{

    private AppBarConfiguration mAppBarConfiguration;
    Intent signInIntent;
    String dbName = SQLiteDbQueries.DATABASE_NAME;
    String APP_DRIVE_DIR = CustomConstants.DRIVE_APP_DIR;
    private ActivityMainBinding binding;
    SharedViewModel sharedViewModel;
    ActivityResultLauncher<Intent> signInLauncher;
    String CUSTOM_LOG_TAG = CustomConstants.CUSTOM_LOG_TAG;
    private ActionBarDrawerToggle toggle;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==CustomConstants.NOTES_NOTIFICATION_PERMISSION_REQUEST_CODE && grantResults[0]== PackageManager.PERMISSION_GRANTED)
        {
            //notes notification permission granted
            createNotificationChannels();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        toggle = new ActionBarDrawerToggle(this, drawer,binding.appBarMain.toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_notes , R.id.nav_daily_logs, R.id.nav_routines)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // You cannot post notifications. Ask for permission.
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    CustomConstants.NOTES_NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }

        createNotificationChannels();

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        new GoogleDriveUtils(MainActivity.this); //this is important dont delete
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intentData = result.getData();
                        try {
                            Drive googleDriveService = GoogleDriveUtils.getDriveService(this,intentData);
                            GoogleDriveUtils.downloadFileSaveAndUpdateDBlocalFile(dbName,
                                    googleDriveService,
                                    dbName,APP_DRIVE_DIR,
                                    this,
                                    dbName,
                                    dbName);

                        } catch (Exception e) {
                            // Google Sign In failed
                            Log.w(CUSTOM_LOG_TAG, "", e);
                        }
                    }
                }
        );

        GoogleSignInClient client = GoogleDriveUtils.signInGoogleAndGetClient(this);
        signInIntent = client.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void startNotificationSchedule() {
        Intent notesIntent = new Intent(this, NotesReminderReceiver.class);
        notesIntent.putExtra(SOURCE, MAIN_ACTIVITY);
        sendBroadcast(notesIntent);
        Intent eyeCareIntent = new Intent(this, EyeCareReceiver.class);
        eyeCareIntent.putExtra(SOURCE, MAIN_ACTIVITY);
        sendBroadcast(eyeCareIntent);
    }

    @Override
    public void setOnDriveUpdatedLocalDBListener() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(MainActivity.this, "updated", Toast.LENGTH_SHORT).show();
//            }
//        });
        sharedViewModel.setDbUpdated(true);
        startNotificationSchedule();
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

    private void createNotificationChannel(CharSequence name, String description, int importance, String channelId) {
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}