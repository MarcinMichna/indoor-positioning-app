package pl.michnam.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;

import pl.michnam.app.config.AppConfig;
import pl.michnam.app.util.Tag;

public class AreaCreationActivity extends AppCompatActivity {
    private boolean areaScanActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_creation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        areaScanActive = true;
        setupWifiScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        areaScanActive = false;
    }

    ///////////////////////////
    ///// VIEW CONTROLLER /////
    ///////////////////////////
    public void onFinishClicked(View v) {
        areaScanActive = false;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /////////////////////
    ///// WIFI SCAN /////
    /////////////////////
    public void setupWifiScan() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean noError = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (noError) {
                    List<ScanResult> results = wifiManager.getScanResults();
                    handleScanResults(results);
                } else Log.w(Tag.WIFI, "WIFI AREA - error while receiving scan results");
                if (areaScanActive) scanLoop();
                else {
                    Log.i(Tag.WIFI, "WIFI AREA - Stopping Scan");
                    unregisterReceiver(this);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);
        Log.i(Tag.WIFI, "WIFI AREA - Starting scan");
        if (areaScanActive) scanLoop();
    }

    private  void scanLoop() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        boolean successfulScan = wifiManager.startScan(); // wifiScanReceiver.onReceive after scan
                        if (!successfulScan) Log.i(Tag.WIFI, "WIFI AREA - Scan while starting scanning");
                    }
                },
                AppConfig.wifiAreaScanWaitTime
        );

    }

    private void handleScanResults(List<ScanResult> results) {
        Log.d(Tag.WIFI, "WIFI AREA - scanned " + results.size() + " devices");
    }
}