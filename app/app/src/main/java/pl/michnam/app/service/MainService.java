package pl.michnam.app.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import pl.michnam.app.MainActivity;
import pl.michnam.app.R;

import static pl.michnam.app.App.CHANNEL_ID;

public class MainService extends Service {
    private static String TAG = "inposService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service starting");
        pushForeground();
        Log.i(TAG, "Service started");
        return START_NOT_STICKY;
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MainService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MainService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void pushForeground()
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TMP TITLE")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent).build();

        startForeground(364, notification);
    }


    public void test()
    {
        Log.d(TAG, "Service bind OK");
    }
}
