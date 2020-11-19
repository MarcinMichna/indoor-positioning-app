package pl.michnam.app.core.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import pl.michnam.app.App;
import pl.michnam.app.config.AppConfig;
import pl.michnam.app.core.activity.MainActivity;
import pl.michnam.app.R;
import pl.michnam.app.scan.WifiScan;

import static pl.michnam.app.App.CHANNEL_ID;

public class MainService extends Service {
    private static boolean working;

    private ServiceCallbacks serviceCallbacks;
    private final IBinder binder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pushServiceForeground();
        return START_NOT_STICKY;
    }

    public void startScan() {
        working = true;
        WifiScan.startWifiScan(this, serviceCallbacks);
    }

    public void stopScan() {
        working = false;
    }

    /////////////////////////////////
    /////// SERVICE UTILS ///////////
    /////////////////////////////////
    public void setServiceCallbacks(ServiceCallbacks serviceCallbacks) {
        this.serviceCallbacks = serviceCallbacks;
    }

    public class LocalBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void pushServiceForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.defaultNotificationText))
                .setSmallIcon(R.drawable.flag)
                .setContentIntent(pendingIntent).build();

        startForeground(AppConfig.mainNotificationId, notification);
    }

    ///////////////////////////
    ///// GETTERS SETTERS /////
    ///////////////////////////

    public static boolean isWorking() {
        return working;
    }
}
