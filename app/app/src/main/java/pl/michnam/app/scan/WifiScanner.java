package pl.michnam.app.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import pl.michnam.app.MainActivity;

public class WifiScanner {

    public static void wifiScan(Context context){

        WifiManager wifiManager = (WifiManager)
                context.getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    List<ScanResult> results = wifiManager.getScanResults();
                    for(ScanResult i : results) {
                        Log.i(MainActivity.TAG,"SSID: " + i.SSID + ", RSSI: " + i.level);
                    }
                } else {
                    // scan failure handling
                    Log.i(MainActivity.TAG, "Wifi scan error");
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            Log.i(MainActivity.TAG, "Wifi scan error");
        }

    }

}
