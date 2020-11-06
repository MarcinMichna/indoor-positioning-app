package pl.michnam.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;

import pl.michnam.app.scan.WifiScan;
import pl.michnam.app.service.MainService;
import pl.michnam.app.service.ServiceCallbacks;

public class MainActivity extends AppCompatActivity implements ServiceCallbacks {
    private String TAG = "inposMain";

    private MainService mainService;
    boolean serviceBound = false;

    private TextView textView;
    private Button button;
    private EditText areaName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startServiceIfNotRunning();
        requestPermission();
    }

    private void init() {
        Log.i(TAG, "Permission check: OK");
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        areaName = findViewById(R.id.areaName);
        button.setText("Add new area");
        areaName.setHint("New area name");
        Intent intent = new Intent(this, MainService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceBound) {
            serviceBound = false;
            mainService.setServiceCallbacks(null);
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "Permission requestCode: " + requestCode);
        Log.d(TAG, Arrays.asList(grantResults).get(0)[0] + "");
        if (requestCode == 1) {
            if (Arrays.asList(grantResults).get(0)[0] != PackageManager.PERMISSION_DENIED) init();
            else requestPermission();
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            init();
        else requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        buildAlertMessageNoGps();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MainService.LocalBinder binder = (MainService.LocalBinder) service;
            mainService = binder.getService();
            serviceBound = true;
            mainService.setServiceCallbacks(MainActivity.this);
            afterSetup();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };

    private void afterSetup() {
        mainService.test();
        WifiScan.setupWifiScan(this, null);
        WifiScan.scan(this);
    }

    //SERVICE
    private void startServiceIfNotRunning() {
        Log.d(TAG, "MainActivity -> startServiceIfNotRunning");
        if (!isServiceRunning()) {
            Log.d(TAG, "Starting service");
            Intent serviceIntent = new Intent(this, MainService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if ("pl.michnam.app.service.MainService".equals(service.service.getClassName()))
                return true;
        return false;
    }

    @Override
    public void debug() {
        Log.d(TAG, "Sevice callbacks OK");
        textView.setText("Service callbacks test");
        mainService.startScan();
    }

    @Override
    public void printWifi(String msg) {
        textView.setText(msg);
    }
}