package pl.michnam.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;

import pl.michnam.app.service.MainService;
import pl.michnam.app.service.ServiceCallbacks;
import pl.michnam.app.util.Tag;

public class MainActivity extends AppCompatActivity implements ServiceCallbacks {
    private MainService mainService;
    boolean boundService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        handlePermissions(); // if OK, runs onReady
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindServiceConnection();
    }

    private void onReady() {

    }

    ////////////////////////////
    ////////// SERVICE /////////
    ////////////////////////////

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MainService.LocalBinder binder = (MainService.LocalBinder) service;
            mainService = binder.getService();
            boundService = true;
            mainService.setServiceCallbacks(MainActivity.this);
            enableButtons();
            onReady();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundService = false;
        }
    };




    private void bindServiceConnection() {
        Intent intent = new Intent(this, MainService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindServiceConnection() {
        if (boundService) {
            boundService = false;
            mainService.setServiceCallbacks(null);
            unbindService(serviceConnection);
        }
    }

    //////////////////////////////
    //// PERMISSIONS HANDLING ////
    //////////////////////////////

    private void handlePermissions(){
        requestPermissionIfNeeded();
        handleLocation();
    }

    private void requestPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) bindServiceConnection();
        else requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); // Runs onRequestPermissionsResult after user action
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(Tag.UI, "Permission requestCode: " + requestCode);
        Log.d(Tag.UI, Arrays.asList(grantResults).get(0)[0] + "");
        if (requestCode == 1) {
            if (Arrays.asList(grantResults).get(0)[0] != PackageManager.PERMISSION_DENIED) bindServiceConnection();
            else requestPermissionIfNeeded();
        }
    }

    public void handleLocation() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) showAlertMessageNoGps();
    }

    private void showAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS to continue")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /////////////////
    ///// VIEW //////
    /////////////////
    private Button startButton;
    private Button areaButton;
    private TextView debugInfo;
    private EditText areaName;

    private void initView() {
        startButton = findViewById(R.id.startButton);
        areaButton = findViewById(R.id.areaButton);
        debugInfo = findViewById(R.id.debugInfo);
        areaName = findViewById(R.id.areaName);

        areaName.setActivated(false);
        startButton.setActivated(false);
    }

    ////////////////////////////
    ///// VIEW CONTROLLER //////
    ////////////////////////////
    public void onStartButtonClick(View v) {
        adjustStartButtonText();
        if (MainService.isWorking()) mainService.startWifiScan();
        else debugInfo.setText("");
    }

    public void onAddAreaButtonClick(View v) {

    }

    /////////////////////////
    ///// VIEW HELPERS //////
    /////////////////////////
    private void adjustStartButtonText() {
        if (MainService.isWorking()) {
            startButton.setText(R.string.start);
            MainService.setWorking(false);
        }
        else {
            startButton.setText(R.string.stop);
            MainService.setWorking(true);
        }
    }

    private void enableButtons() {
        startButton.setActivated(true);
        areaButton.setActivated(true);
    }

    @Override
    public void setDebugMessage(String msg) {
        if (MainService.isWorking())debugInfo.setText(msg);
    }

}