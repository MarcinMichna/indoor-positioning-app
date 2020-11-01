package pl.michnam.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;

import pl.michnam.app.scan.WifiScan;
import pl.michnam.app.service.MainService;

public class MainActivity extends AppCompatActivity {
    private String TAG = "inposMain";

    private MainService mainService;
    boolean serviceBound = false;

    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startServiceIfNotRunning();
        requestPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity -> onStart");
        Intent intent = new Intent(this, MainService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) init();
        else requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void init() {
        Log.i(TAG, "Permission check: OK");
        //mainService.test();
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MainService.LocalBinder binder = (MainService.LocalBinder) service;
            mainService =  binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };

    //SERVICE
    private void startServiceIfNotRunning(){
        Log.d(TAG, "MainActivity -> startServiceIfNotRunning");
        if(!isServiceRunning())
        {
            Log.d(TAG, "Starting service");
            Intent serviceIntent = new Intent(this, MainService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("pl.michnam.app.service.MainService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}