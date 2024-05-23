package com.example.mos.appStateTracker;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppStateTracker extends Application implements Application.ActivityLifecycleCallbacks {
    private static boolean isVisible;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        isVisible = true;
//        Toast.makeText(activity.getApplicationContext(), "visible", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        isVisible = false;
//        Toast.makeText(activity.getApplicationContext(), "not visible", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    public static boolean isVisible() {
        return isVisible;
    }
}
