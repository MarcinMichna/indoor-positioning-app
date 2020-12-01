package pl.michnam.app;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.content.ContextCompat;

import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.service.MainService;
import pl.michnam.app.util.Pref;
import pl.michnam.app.util.Tag;

public class App extends Application {
    private static App instance;
    public static final String CHANNEL_ID = "MainChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        createNotificationChannel();
        startServiceIfNotRunning();
        checkPreferences();
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Indoor Positioning", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    private void startServiceIfNotRunning() {
        if (!isServiceRunning()) {
            Log.d(Tag.CORE, "Starting service");
            Intent serviceIntent = new Intent(this, MainService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if ("pl.michnam.app.core.service.MainService".equals(service.service.getClassName()))
                return true;
        return false;
    }

    private void checkPreferences(){
        SharedPreferences sharedPref = getSharedPreferences(Pref.prefFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        if (!sharedPref.contains(Pref.initialized)){
            editor = sharedPref.edit();
            editor.putBoolean(Pref.initialized, true);
            editor.putBoolean(Pref.activeMode, false);
            editor.putInt(Pref.margin, AppConfig.marginSignalStrength);
            editor.putInt(Pref.scanAge, AppConfig.maxScanAge);
            editor.apply();
            Log.i(Tag.CORE, "Set default preferences");
        }
    }

    public static App getInstance() {
        return instance;
    }
}
