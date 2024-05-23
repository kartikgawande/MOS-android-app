package com.example.mos.ui.dailyLogs;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mos.databinding.FragmentDailyLogsBinding;
import com.example.mos.googleDrive.GoogleDriveUtils;
import com.example.mos.sqlite.DBUtils;
import com.example.mos.sqlite.SQLiteDbQueries;
import com.example.mos.ui.SharedViewModel;
import com.example.mos.ui.dailyLogs.dailyLogRV.DailyLogModel;
import com.example.mos.ui.dailyLogs.dailyLogRV.DailyLogsRVadapter;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.services.drive.Drive;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class DailyLogsFragment extends Fragment {

    final ArrayList<DailyLogModel> dailyLogs = new ArrayList<>();
    private FragmentDailyLogsBinding binding;
    DailyLogsRVadapter dailyLogsRVadapter;
    SQLiteDatabase dbw;
    SQLiteDbQueries dbQueries;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDailyLogsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.isDbUpdated().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    updateDailyLogsRV();
                }
            }
        });

        FloatingActionButton dailyLogAddFAB = binding.dailyLogAddFAB;
        dbQueries = new SQLiteDbQueries(requireContext());

        dbw = dbQueries.getWritableDatabase();

        RecyclerView dailyLogsRV = binding.dailyLogsRV;
        dailyLogsRVadapter = new DailyLogsRVadapter(requireContext(), dailyLogs);
        dailyLogsRV.setLayoutManager(new LinearLayoutManager(requireContext()));
        dailyLogsRV.setAdapter(dailyLogsRVadapter);

        ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intentData = result.getData();
                        try {
                            Drive googleDriveService = GoogleDriveUtils.getDriveService(requireContext(),intentData);
                            DBUtils.backupDBtoDrive(requireContext(), SQLiteDbQueries.DATABASE_NAME,googleDriveService);
                        } catch (ApiException e) {
                            // Google Sign In failed
                            Log.w("CustomTag", "Google sign in failed:", e);
                        }
                    }
                }
        );

        dailyLogAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbQueries.addDailyLog(dbw,getCurrentDay(),getCurrentDate(),getCurrentMonth(),getCurrentYear());

                signInAndBackupDBtoDrive(signInLauncher);

                updateDailyLogsRV();
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDailyLogsRV();
    }

    private void updateDailyLogsRV() {
        dailyLogs.clear();
        dailyLogs.addAll(dbQueries.getAllDailyLogs(dbw));
        Collections.reverse(dailyLogs);
        dailyLogsRVadapter.notifyDataSetChanged();
    }

    public static String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        return days[dayOfWeek - 1];  // Note: Calendar.DAY_OF_WEEK returns values 1-7 (Sun-Sat)
    }

    public static String getCurrentDate(){
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd", Locale.getDefault());
        return date.format(formatter);
    }
    public static String getCurrentMonth(){
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM", Locale.getDefault());
        return date.format(formatter);
    }
    public static String getCurrentYear(){
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy", Locale.getDefault());
        return date.format(formatter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void signInAndBackupDBtoDrive(ActivityResultLauncher<Intent> signInLauncher) {
        GoogleSignInClient client = GoogleDriveUtils.signInGoogleAndGetClient(requireContext());
        Intent signInIntent = client.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }
}