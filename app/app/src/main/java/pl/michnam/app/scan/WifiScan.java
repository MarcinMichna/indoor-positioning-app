package pl.michnam.app.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

import pl.michnam.app.MainActivity;

public class WifiScan {
    private static String TAG = "inposScan";

    public static void setupWifiScan(Context context, TextView textView){

        WifiManager wifiManager = (WifiManager)
                context.getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    List<ScanResult> results = wifiManager.getScanResults();
                    textView.setText("");
                    for(ScanResult i : results) {
                        String info = "SSID: " + i.SSID + ", RSSI: " + i.level;
                        Log.d(TAG,info);
                        textView.setText(textView.getText() + info + "\n");
                    }
                } else {
                    // scan failure handling
                    Log.i(TAG, "Wifi scan error");
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

    }

    public static void scan(Context context) {
        WifiManager wifiManager = (WifiManager)
                context.getSystemService(Context.WIFI_SERVICE);

        boolean success = wifiManager.startScan();
        if (!success) {
            Log.i(TAG, "Wifi scan error");
        }
    }
}
